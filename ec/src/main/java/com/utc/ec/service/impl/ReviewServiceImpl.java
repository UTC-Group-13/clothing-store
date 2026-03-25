package com.utc.ec.service.impl;

import com.utc.ec.dto.CreateReviewRequest;
import com.utc.ec.dto.ReviewResponseDTO;
import com.utc.ec.dto.ReviewSummaryDTO;
import com.utc.ec.entity.*;
import com.utc.ec.exception.BusinessException;
import com.utc.ec.exception.ResourceNotFoundException;
import com.utc.ec.repository.*;
import com.utc.ec.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final UserReviewRepository   reviewRepository;
    private final OrderLineRepository    orderLineRepository;
    private final ShopOrderRepository    orderRepository;
    private final OrderStatusRepository  orderStatusRepository;
    private final VariantStockRepository variantStockRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository      productRepository;
    private final ColorRepository        colorRepository;
    private final SizeRepository         sizeRepository;
    private final SiteUserRepository     userRepository;

    // =====================================================================
    //  Tao review
    // =====================================================================

    @Override
    @Transactional
    public ReviewResponseDTO createReview(String username, CreateReviewRequest request) {
        SiteUser user = getUser(username);

        // 1. Kiem tra order_line ton tai
        OrderLine orderLine = orderLineRepository.findById(request.getOrderedProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "review.orderLineNotFound", request.getOrderedProductId()));

        // 2. Kiem tra order_line thuoc ve user
        ShopOrder order = orderRepository.findById(orderLine.getOrderId())
                .orElseThrow(() -> new BusinessException("order.notFound", orderLine.getOrderId()));

        if (!order.getUserId().equals(user.getId())) {
            throw new BusinessException("review.notYourOrder");
        }

        // 3. Kiem tra don hang da DELIVERED
        OrderStatus status = orderStatusRepository.findById(order.getOrderStatus()).orElse(null);
        if (status == null
                || !OrderStatusServiceImpl.STATUS_DELIVERED.equalsIgnoreCase(status.getStatus())) {
            throw new BusinessException("review.orderNotDelivered");
        }

        // 4. Kiem tra user da review order_line nay chua
        if (reviewRepository.existsByUserIdAndOrderedProductId(user.getId(), request.getOrderedProductId())) {
            throw new BusinessException("review.alreadyReviewed");
        }

        // 5. Tao review
        UserReview review = new UserReview();
        review.setUserId(user.getId());
        review.setOrderedProductId(request.getOrderedProductId());
        review.setRatingValue(request.getRatingValue());
        review.setComment(request.getComment());

        review = reviewRepository.save(review);

        return buildResponseDTO(review, user.getUsername());
    }

    // =====================================================================
    //  Xem review theo san pham
    // =====================================================================

    @Override
    public List<ReviewResponseDTO> getReviewsByProductId(Integer productId) {
        // Validate product ton tai
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("product.notFound", productId);
        }

        List<UserReview> reviews = reviewRepository.findByProductId(productId);
        if (reviews.isEmpty()) return Collections.emptyList();

        return buildResponseDTOList(reviews);
    }

    @Override
    public ReviewSummaryDTO getReviewSummary(Integer productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("product.notFound", productId);
        }

        Double avg = reviewRepository.avgRatingByProductId(productId);
        Integer count = reviewRepository.countByProductId(productId);

        return ReviewSummaryDTO.builder()
                .productId(productId)
                .avgRating(avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0)
                .totalReviews(count != null ? count : 0)
                .build();
    }

    // =====================================================================
    //  Xem review cua toi
    // =====================================================================

    @Override
    public List<ReviewResponseDTO> getMyReviews(String username) {
        SiteUser user = getUser(username);
        List<UserReview> reviews = reviewRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        if (reviews.isEmpty()) return Collections.emptyList();

        return buildResponseDTOList(reviews);
    }

    // =====================================================================
    //  Xoa review
    // =====================================================================

    @Override
    @Transactional
    public void deleteReview(String username, Integer reviewId) {
        SiteUser user = getUser(username);

        UserReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("review.notFound", reviewId));

        if (!review.getUserId().equals(user.getId())) {
            throw new BusinessException("review.notYourReview");
        }

        reviewRepository.delete(review);
    }

    // =====================================================================
    //  Private helpers
    // =====================================================================

    private SiteUser getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("auth.user.notFound"));
    }

    /**
     * Build single review response (dung khi vua tao xong)
     */
    private ReviewResponseDTO buildResponseDTO(UserReview review, String username) {
        ReviewResponseDTO.ReviewResponseDTOBuilder builder = ReviewResponseDTO.builder()
                .id(review.getId())
                .userId(review.getUserId())
                .username(username)
                .orderedProductId(review.getOrderedProductId())
                .ratingValue(review.getRatingValue())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt());

        // Enrich thong tin san pham tu order_line
        orderLineRepository.findById(review.getOrderedProductId()).ifPresent(line ->
                enrichFromOrderLine(builder, line));

        return builder.build();
    }

    /**
     * Build list review response (batch load de tranh N+1)
     */
    private List<ReviewResponseDTO> buildResponseDTOList(List<UserReview> reviews) {
        // Collect tat ca userIds de batch load usernames
        List<Integer> userIds = reviews.stream()
                .map(UserReview::getUserId).distinct().toList();
        Map<Integer, String> usernameMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(SiteUser::getId, SiteUser::getUsername));

        // Collect tat ca orderLineIds de batch load
        List<Integer> orderLineIds = reviews.stream()
                .map(UserReview::getOrderedProductId).distinct().toList();
        Map<Integer, OrderLine> orderLineMap = orderLineRepository.findAllById(orderLineIds).stream()
                .collect(Collectors.toMap(OrderLine::getId, Function.identity()));

        // Collect variantStockIds
        List<Integer> stockIds = orderLineMap.values().stream()
                .map(OrderLine::getVariantStockId).distinct().toList();
        Map<Integer, VariantStock> stockMap = variantStockRepository.findAllById(stockIds).stream()
                .collect(Collectors.toMap(VariantStock::getId, Function.identity()));

        // Collect variantIds
        List<Integer> variantIds = stockMap.values().stream()
                .map(VariantStock::getVariantId).distinct().toList();
        Map<Integer, ProductVariant> variantMap = productVariantRepository.findAllById(variantIds).stream()
                .collect(Collectors.toMap(ProductVariant::getId, Function.identity()));

        // Collect productIds
        List<Integer> productIds = variantMap.values().stream()
                .map(ProductVariant::getProductId).distinct().toList();
        Map<Integer, Product> productMap = productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        // Collect colorIds
        List<Integer> colorIds = variantMap.values().stream()
                .map(ProductVariant::getColorId).distinct().toList();
        Map<Integer, Color> colorMap = colorRepository.findAllById(colorIds).stream()
                .collect(Collectors.toMap(Color::getId, Function.identity()));

        // Collect sizeIds
        List<Integer> sizeIds = stockMap.values().stream()
                .map(VariantStock::getSizeId).distinct().toList();
        Map<Integer, Size> sizeMap = sizeRepository.findAllById(sizeIds).stream()
                .collect(Collectors.toMap(Size::getId, Function.identity()));

        // Build DTOs
        return reviews.stream().map(review ->
                buildSingleFromMaps(review, usernameMap, orderLineMap, stockMap,
                        variantMap, productMap, colorMap, sizeMap)
        ).toList();
    }

    /**
     * Build single DTO tu cac pre-loaded maps (giam cognitive complexity)
     */
    private ReviewResponseDTO buildSingleFromMaps(
            UserReview review,
            Map<Integer, String> usernameMap,
            Map<Integer, OrderLine> orderLineMap,
            Map<Integer, VariantStock> stockMap,
            Map<Integer, ProductVariant> variantMap,
            Map<Integer, Product> productMap,
            Map<Integer, Color> colorMap,
            Map<Integer, Size> sizeMap) {

        ReviewResponseDTO.ReviewResponseDTOBuilder builder = ReviewResponseDTO.builder()
                .id(review.getId())
                .userId(review.getUserId())
                .username(usernameMap.getOrDefault(review.getUserId(), "Unknown"))
                .orderedProductId(review.getOrderedProductId())
                .ratingValue(review.getRatingValue())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt());

        OrderLine line = orderLineMap.get(review.getOrderedProductId());
        if (line != null) {
            enrichBuilderFromLine(builder, line, stockMap, variantMap, productMap, colorMap, sizeMap);
        }
        return builder.build();
    }

    /**
     * Enrich builder tu OrderLine + pre-loaded maps
     */
    private void enrichBuilderFromLine(
            ReviewResponseDTO.ReviewResponseDTOBuilder builder,
            OrderLine line,
            Map<Integer, VariantStock> stockMap,
            Map<Integer, ProductVariant> variantMap,
            Map<Integer, Product> productMap,
            Map<Integer, Color> colorMap,
            Map<Integer, Size> sizeMap) {

        VariantStock stock = stockMap.get(line.getVariantStockId());
        if (stock == null) return;

        builder.sku(stock.getSku());

        ProductVariant variant = variantMap.get(stock.getVariantId());
        if (variant != null) {
            builder.colorImageUrl(variant.getColorImageUrl());

            Product product = productMap.get(variant.getProductId());
            if (product != null) {
                builder.productId(product.getId())
                        .productName(product.getName())
                        .productSlug(product.getSlug());
            }
            Color color = colorMap.get(variant.getColorId());
            if (color != null) {
                builder.colorName(color.getName()).colorHex(color.getHexCode());
            }
        }
        Size size = sizeMap.get(stock.getSizeId());
        if (size != null) {
            builder.sizeLabel(size.getLabel()).sizeType(size.getType());
        }
    }

    /**
     * Enrich thong tin san pham tu 1 order_line (dung cho single review)
     */
    private void enrichFromOrderLine(ReviewResponseDTO.ReviewResponseDTOBuilder builder, OrderLine line) {
        variantStockRepository.findById(line.getVariantStockId()).ifPresent(stock -> {
            builder.sku(stock.getSku());

            productVariantRepository.findById(stock.getVariantId()).ifPresent(variant -> {
                builder.colorImageUrl(variant.getColorImageUrl());

                productRepository.findById(variant.getProductId()).ifPresent(product ->
                        builder.productId(product.getId())
                                .productName(product.getName())
                                .productSlug(product.getSlug()));

                colorRepository.findById(variant.getColorId()).ifPresent(color ->
                        builder.colorName(color.getName()).colorHex(color.getHexCode()));
            });

            sizeRepository.findById(stock.getSizeId()).ifPresent(size ->
                    builder.sizeLabel(size.getLabel()).sizeType(size.getType()));
        });
    }
}



