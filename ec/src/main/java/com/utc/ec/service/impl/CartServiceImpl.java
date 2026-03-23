package com.utc.ec.service.impl;

import com.utc.ec.dto.CartItemDetailDTO;
import com.utc.ec.dto.CartItemRequest;
import com.utc.ec.dto.CartSummaryDTO;
import com.utc.ec.entity.*;
import com.utc.ec.exception.BusinessException;
import com.utc.ec.exception.ResourceNotFoundException;
import com.utc.ec.repository.*;
import com.utc.ec.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final ShoppingCartRepository cartRepository;
    private final ShoppingCartItemRepository cartItemRepository;
    private final VariantStockRepository variantStockRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;
    private final ColorRepository colorRepository;
    private final SizeRepository sizeRepository;
    private final SiteUserRepository userRepository;

    // =========================================================
    //  Public methods
    // =========================================================

    @Override
    public CartSummaryDTO getMyCart(String username) {
        ShoppingCart cart = getOrCreateCart(username);
        return buildCartSummary(cart);
    }

    @Override
    @Transactional
    public CartSummaryDTO addItem(String username, CartItemRequest request) {
        ShoppingCart cart = getOrCreateCart(username);

        VariantStock stock = variantStockRepository.findById(request.getVariantStockId())
                .orElseThrow(() -> new ResourceNotFoundException("cart.stockNotFound", request.getVariantStockId()));

        if (stock.getStockQty() < request.getQty()) {
            throw new BusinessException("cart.notEnoughStock", stock.getSku(), stock.getStockQty());
        }

        // Neu item da co trong gio → cong them
        Optional<ShoppingCartItem> existing =
                cartItemRepository.findByCartIdAndVariantStockId(cart.getId(), request.getVariantStockId());

        if (existing.isPresent()) {
            ShoppingCartItem item = existing.get();
            int newQty = item.getQty() + request.getQty();
            if (newQty > stock.getStockQty()) {
                throw new BusinessException("cart.notEnoughStock", stock.getSku(), stock.getStockQty());
            }
            item.setQty(newQty);
            cartItemRepository.save(item);
        } else {
            ShoppingCartItem item = new ShoppingCartItem();
            item.setCartId(cart.getId());
            item.setVariantStockId(request.getVariantStockId());
            item.setQty(request.getQty());
            cartItemRepository.save(item);
        }

        return buildCartSummary(cart);
    }

    @Override
    @Transactional
    public CartSummaryDTO updateItem(String username, Integer itemId, CartItemRequest request) {
        ShoppingCart cart = getOrCreateCart(username);

        ShoppingCartItem item = cartItemRepository.findByIdAndCartId(itemId, cart.getId())
                .orElseThrow(() -> new ResourceNotFoundException("cart.itemNotFound", itemId));

        VariantStock stock = variantStockRepository.findById(item.getVariantStockId())
                .orElseThrow(() -> new ResourceNotFoundException("cart.stockNotFound", item.getVariantStockId()));

        if (request.getQty() > stock.getStockQty()) {
            throw new BusinessException("cart.notEnoughStock", stock.getSku(), stock.getStockQty());
        }

        item.setQty(request.getQty());
        cartItemRepository.save(item);

        return buildCartSummary(cart);
    }

    @Override
    @Transactional
    public CartSummaryDTO removeItem(String username, Integer itemId) {
        ShoppingCart cart = getOrCreateCart(username);

        ShoppingCartItem item = cartItemRepository.findByIdAndCartId(itemId, cart.getId())
                .orElseThrow(() -> new ResourceNotFoundException("cart.itemNotFound", itemId));

        cartItemRepository.delete(item);
        return buildCartSummary(cart);
    }

    @Override
    @Transactional
    public void clearCart(String username) {
        ShoppingCart cart = getOrCreateCart(username);
        cartItemRepository.deleteByCartId(cart.getId());
    }

    // =========================================================
    //  Private helpers
    // =========================================================

    /**
     * Lay gio hang cua user, neu chua co thi tu dong tao moi.
     */
    private ShoppingCart getOrCreateCart(String username) {
        SiteUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("auth.user.notFound"));

        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    ShoppingCart newCart = new ShoppingCart();
                    newCart.setUserId(user.getId());
                    return cartRepository.save(newCart);
                });
    }

    /**
     * Tao CartSummaryDTO tu ShoppingCart, batch-load toan bo thong tin lien quan.
     */
    private CartSummaryDTO buildCartSummary(ShoppingCart cart) {
        List<ShoppingCartItem> items = cartItemRepository.findByCartId(cart.getId());

        if (items.isEmpty()) {
            CartSummaryDTO summary = new CartSummaryDTO();
            summary.setCartId(cart.getId());
            summary.setUserId(cart.getUserId());
            summary.setItems(Collections.emptyList());
            summary.setTotalItems(0);
            summary.setTotalAmount(BigDecimal.ZERO);
            return summary;
        }

        // --- Batch load ---
        List<Integer> stockIds = items.stream()
                .map(ShoppingCartItem::getVariantStockId)
                .distinct().collect(Collectors.toList());

        Map<Integer, VariantStock> stockMap = variantStockRepository.findAllById(stockIds)
                .stream().collect(Collectors.toMap(VariantStock::getId, Function.identity()));

        List<Integer> variantIds = stockMap.values().stream()
                .map(VariantStock::getVariantId)
                .distinct().collect(Collectors.toList());

        Map<Integer, ProductVariant> variantMap = productVariantRepository.findAllById(variantIds)
                .stream().collect(Collectors.toMap(ProductVariant::getId, Function.identity()));

        List<Integer> productIds = variantMap.values().stream()
                .map(ProductVariant::getProductId)
                .distinct().collect(Collectors.toList());

        Map<Integer, Product> productMap = productRepository.findAllById(productIds)
                .stream().collect(Collectors.toMap(Product::getId, Function.identity()));

        List<Integer> colorIds = variantMap.values().stream()
                .map(ProductVariant::getColorId)
                .distinct().collect(Collectors.toList());

        Map<Integer, Color> colorMap = colorRepository.findAllById(colorIds)
                .stream().collect(Collectors.toMap(Color::getId, Function.identity()));

        List<Integer> sizeIds = stockMap.values().stream()
                .map(VariantStock::getSizeId)
                .distinct().collect(Collectors.toList());

        Map<Integer, Size> sizeMap = sizeRepository.findAllById(sizeIds)
                .stream().collect(Collectors.toMap(Size::getId, Function.identity()));

        // --- Map sang DTO ---
        List<CartItemDetailDTO> detailList = items.stream()
                .map(item -> toDetailDTO(item, stockMap, variantMap, productMap, colorMap, sizeMap))
                .collect(Collectors.toList());

        BigDecimal totalAmount = detailList.stream()
                .map(CartItemDetailDTO::getSubtotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        CartSummaryDTO summary = new CartSummaryDTO();
        summary.setCartId(cart.getId());
        summary.setUserId(cart.getUserId());
        summary.setItems(detailList);
        summary.setTotalItems(detailList.stream().mapToInt(CartItemDetailDTO::getQty).sum());
        summary.setTotalAmount(totalAmount);
        return summary;
    }

    private CartItemDetailDTO toDetailDTO(ShoppingCartItem item,
                                          Map<Integer, VariantStock> stockMap,
                                          Map<Integer, ProductVariant> variantMap,
                                          Map<Integer, Product> productMap,
                                          Map<Integer, Color> colorMap,
                                          Map<Integer, Size> sizeMap) {

        CartItemDetailDTO dto = new CartItemDetailDTO();
        dto.setId(item.getId());
        dto.setCartId(item.getCartId());
        dto.setVariantStockId(item.getVariantStockId());
        dto.setQty(item.getQty());

        VariantStock stock = stockMap.get(item.getVariantStockId());
        if (stock != null) {
            dto.setSku(stock.getSku());
            dto.setAvailableStock(stock.getStockQty());

            ProductVariant variant = variantMap.get(stock.getVariantId());
            if (variant != null) {
                dto.setVariantId(variant.getId());
                dto.setColorImageUrl(variant.getColorImageUrl());

                Product product = productMap.get(variant.getProductId());
                if (product != null) {
                    dto.setProductId(product.getId());
                    dto.setProductName(product.getName());
                    dto.setProductSlug(product.getSlug());

                    // Don gia: price_override neu co, nguoc lai dung base_price
                    BigDecimal unitPrice = (stock.getPriceOverride() != null)
                            ? stock.getPriceOverride()
                            : product.getBasePrice();
                    dto.setUnitPrice(unitPrice);
                    dto.setSubtotal(unitPrice.multiply(BigDecimal.valueOf(item.getQty())));
                }

                Color color = colorMap.get(variant.getColorId());
                if (color != null) {
                    dto.setColorName(color.getName());
                    dto.setColorHex(color.getHexCode());
                }
            }

            Size size = sizeMap.get(stock.getSizeId());
            if (size != null) {
                dto.setSizeLabel(size.getLabel());
                dto.setSizeType(size.getType());
            }
        }

        return dto;
    }
}

