package com.utc.ec.service.impl;

import com.utc.ec.dto.ProductDetailResponse;
import com.utc.ec.dto.ProductFullRequest;
import com.utc.ec.entity.*;
import com.utc.ec.exception.BusinessException;
import com.utc.ec.exception.ResourceNotFoundException;
import com.utc.ec.repository.*;
import com.utc.ec.service.ProductManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductManagementServiceImpl implements ProductManagementService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final VariantStockRepository stockRepository;
    private final CategoryRepository categoryRepository;
    private final ColorRepository colorRepository;
    private final SizeRepository sizeRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ================================================================
    // CREATE FULL
    // ================================================================

    @Override
    @Transactional
    public ProductDetailResponse createFull(ProductFullRequest request) {

        // 1. Validate category
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("product.categoryNotFound", request.getCategoryId()));

        // 2. Validate slug unique
        if (productRepository.existsBySlug(request.getSlug())) {
            throw new BusinessException("product.slugExists", request.getSlug());
        }

        // 3. Validate chi co 1 bien the isDefault = true
        validateSingleDefault(request.getVariants());

        // 4. Tao Product
        Product product = new Product();
        product.setName(request.getName());
        product.setSlug(request.getSlug());
        product.setDescription(request.getDescription());
        product.setCategoryId(request.getCategoryId());
        product.setBasePrice(request.getBasePrice());
        product.setBrand(request.getBrand());
        product.setMaterial(request.getMaterial());
        product.setIsActive(request.getIsActive() != null ? request.getIsActive() : Boolean.TRUE);
        product = productRepository.save(product);

        // 5. Tao Variants + Stocks
        if (request.getVariants() != null) {
            for (ProductFullRequest.VariantRequest varReq : request.getVariants()) {
                // Validate color ton tai
                if (!colorRepository.existsById(varReq.getColorId())) {
                    throw new ResourceNotFoundException("productVariant.colorNotFound", varReq.getColorId());
                }
                // Tao Variant
                ProductVariant variant = new ProductVariant();
                variant.setProductId(product.getId());
                variant.setColorId(varReq.getColorId());
                variant.setColorImageUrl(varReq.getColorImageUrl());
                variant.setImages(varReq.getImages());
                variant.setIsDefault(Boolean.TRUE.equals(varReq.getIsDefault()));
                variant = variantRepository.save(variant);

                // Tao Stocks cho Variant nay
                if (varReq.getStocks() != null) {
                    createStocks(variant.getId(), varReq.getStocks());
                }
            }
        }

        return buildResponse(product, category);
    }

    // ================================================================
    // UPDATE FULL
    // ================================================================

    @Override
    @Transactional
    public ProductDetailResponse updateFull(Integer productId, ProductFullRequest request) {

        // 1. Tim Product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("product.notFound", productId));

        // 2. Validate category neu co thay doi
        Category category;
        if (request.getCategoryId() != null && !request.getCategoryId().equals(product.getCategoryId())) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("product.categoryNotFound", request.getCategoryId()));
        } else {
            category = categoryRepository.findById(product.getCategoryId()).orElse(null);
        }

        // 3. Validate slug unique neu thay doi
        if (request.getSlug() != null && !request.getSlug().equals(product.getSlug())
                && productRepository.existsBySlugAndIdNot(request.getSlug(), productId)) {
            throw new BusinessException("product.slugExists", request.getSlug());
        }

        // 4. Validate chi co 1 bien the isDefault = true
        validateSingleDefault(request.getVariants());

        // 5. Cap nhat Product fields (chi update field khac null)
        if (request.getName() != null) product.setName(request.getName());
        if (request.getSlug() != null) product.setSlug(request.getSlug());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getCategoryId() != null) product.setCategoryId(request.getCategoryId());
        if (request.getBasePrice() != null) product.setBasePrice(request.getBasePrice());
        if (request.getBrand() != null) product.setBrand(request.getBrand());
        if (request.getMaterial() != null) product.setMaterial(request.getMaterial());
        if (request.getIsActive() != null) product.setIsActive(request.getIsActive());
        product = productRepository.save(product);

        // 6. Xu ly Variants
        if (request.getVariants() != null) {
            for (ProductFullRequest.VariantRequest varReq : request.getVariants()) {
                if (varReq.getId() != null) {
                    // Cap nhat Variant da ton tai
                    updateVariant(varReq, productId);
                } else {
                    // Tao Variant moi
                    createVariant(varReq, productId);
                }
            }
        }

        return buildResponse(product, category);
    }

    // ================================================================
    // GET FULL
    // ================================================================

    @Override
    public ProductDetailResponse getFullById(Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("product.notFound", productId));
        Category category = categoryRepository.findById(product.getCategoryId()).orElse(null);
        return buildResponse(product, category);
    }

    // ================================================================
    // PRIVATE HELPERS
    // ================================================================

    private void validateSingleDefault(List<ProductFullRequest.VariantRequest> variants) {
        if (variants == null) return;
        long defaultCount = variants.stream()
                .filter(v -> Boolean.TRUE.equals(v.getIsDefault()))
                .count();
        if (defaultCount > 1) {
            throw new BusinessException("productVariant.multipleDefault");
        }
    }

    private void createVariant(ProductFullRequest.VariantRequest varReq, Integer productId) {
        if (!colorRepository.existsById(varReq.getColorId())) {
            throw new ResourceNotFoundException("productVariant.colorNotFound", varReq.getColorId());
        }
        if (variantRepository.existsByProductIdAndColorId(productId, varReq.getColorId())) {
            throw new BusinessException("productVariant.duplicateColor");
        }
        ProductVariant variant = new ProductVariant();
        variant.setProductId(productId);
        variant.setColorId(varReq.getColorId());
        variant.setColorImageUrl(varReq.getColorImageUrl());
        variant.setImages(varReq.getImages());
        variant.setIsDefault(Boolean.TRUE.equals(varReq.getIsDefault()));
        variant = variantRepository.save(variant);

        if (varReq.getStocks() != null) {
            createStocks(variant.getId(), varReq.getStocks());
        }
    }

    private void updateVariant(ProductFullRequest.VariantRequest varReq, Integer productId) {
        ProductVariant variant = variantRepository.findById(varReq.getId())
                .orElseThrow(() -> new ResourceNotFoundException("productVariant.notFound", varReq.getId()));

        if (varReq.getColorId() != null && !varReq.getColorId().equals(variant.getColorId())) {
            if (!colorRepository.existsById(varReq.getColorId())) {
                throw new ResourceNotFoundException("productVariant.colorNotFound", varReq.getColorId());
            }
            if (variantRepository.existsByProductIdAndColorIdAndIdNot(productId, varReq.getColorId(), varReq.getId())) {
                throw new BusinessException("productVariant.duplicateColor");
            }
            variant.setColorId(varReq.getColorId());
        }
        if (varReq.getColorImageUrl() != null) variant.setColorImageUrl(varReq.getColorImageUrl());
        if (varReq.getImages() != null) variant.setImages(varReq.getImages());
        if (varReq.getIsDefault() != null) variant.setIsDefault(varReq.getIsDefault());
        variantRepository.save(variant);

        if (varReq.getStocks() != null) {
            processStocks(variant.getId(), varReq.getStocks());
        }
    }

    private void createStocks(Integer variantId, List<ProductFullRequest.StockRequest> stocks) {
        for (ProductFullRequest.StockRequest stockReq : stocks) {
            if (!sizeRepository.existsById(stockReq.getSizeId())) {
                throw new ResourceNotFoundException("variantStock.sizeNotFound", stockReq.getSizeId());
            }
            if (stockRepository.existsBySku(stockReq.getSku())) {
                throw new BusinessException("variantStock.skuExists", stockReq.getSku());
            }
            VariantStock stock = new VariantStock();
            stock.setVariantId(variantId);
            stock.setSizeId(stockReq.getSizeId());
            stock.setStockQty(stockReq.getStockQty() != null ? stockReq.getStockQty() : 0);
            stock.setPriceOverride(stockReq.getPriceOverride());
            stock.setSku(stockReq.getSku());
            stockRepository.save(stock);
        }
    }

    private void processStocks(Integer variantId, List<ProductFullRequest.StockRequest> stocks) {
        for (ProductFullRequest.StockRequest stockReq : stocks) {
            if (stockReq.getId() != null) {
                // Cap nhat Stock da ton tai
                VariantStock stock = stockRepository.findById(stockReq.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("variantStock.notFound", stockReq.getId()));
                if (stockReq.getSizeId() != null) {
                    if (!sizeRepository.existsById(stockReq.getSizeId())) {
                        throw new ResourceNotFoundException("variantStock.sizeNotFound", stockReq.getSizeId());
                    }
                    stock.setSizeId(stockReq.getSizeId());
                }
                if (stockReq.getStockQty() != null) stock.setStockQty(stockReq.getStockQty());
                // priceOverride co the set null (xoa gia override)
                stock.setPriceOverride(stockReq.getPriceOverride());
                if (stockReq.getSku() != null) {
                    if (!stockReq.getSku().equals(stock.getSku())
                            && stockRepository.existsBySkuAndIdNot(stockReq.getSku(), stock.getId())) {
                        throw new BusinessException("variantStock.skuExists", stockReq.getSku());
                    }
                    stock.setSku(stockReq.getSku());
                }
                stockRepository.save(stock);
            } else {
                // Tao Stock moi
                if (!sizeRepository.existsById(stockReq.getSizeId())) {
                    throw new ResourceNotFoundException("variantStock.sizeNotFound", stockReq.getSizeId());
                }
                if (stockRepository.existsBySku(stockReq.getSku())) {
                    throw new BusinessException("variantStock.skuExists", stockReq.getSku());
                }
                VariantStock stock = new VariantStock();
                stock.setVariantId(variantId);
                stock.setSizeId(stockReq.getSizeId());
                stock.setStockQty(stockReq.getStockQty() != null ? stockReq.getStockQty() : 0);
                stock.setPriceOverride(stockReq.getPriceOverride());
                stock.setSku(stockReq.getSku());
                stockRepository.save(stock);
            }
        }
    }

    private ProductDetailResponse buildResponse(Product product, Category category) {
        ProductDetailResponse response = new ProductDetailResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setSlug(product.getSlug());
        response.setDescription(product.getDescription());
        response.setCategoryId(product.getCategoryId());
        response.setCategoryName(category != null ? category.getName() : null);
        response.setBasePrice(product.getBasePrice());
        response.setBrand(product.getBrand());
        response.setMaterial(product.getMaterial());
        response.setIsActive(product.getIsActive());
        if (product.getCreatedAt() != null) response.setCreatedAt(product.getCreatedAt().format(FORMATTER));
        if (product.getUpdatedAt() != null) response.setUpdatedAt(product.getUpdatedAt().format(FORMATTER));

        // Lay danh sach variants cua product
        List<ProductVariant> variants = variantRepository.findByProductId(product.getId());

        // Batch load Colors
        List<Integer> colorIds = variants.stream()
                .map(ProductVariant::getColorId)
                .distinct()
                .collect(Collectors.toList());
        Map<Integer, Color> colorMap = colorRepository.findAllById(colorIds)
                .stream().collect(Collectors.toMap(Color::getId, c -> c));

        List<ProductDetailResponse.VariantDetail> variantDetails = new ArrayList<>();
        for (ProductVariant variant : variants) {
            ProductDetailResponse.VariantDetail vd = new ProductDetailResponse.VariantDetail();
            vd.setId(variant.getId());
            vd.setColorId(variant.getColorId());
            vd.setColorImageUrl(variant.getColorImageUrl());
            vd.setImages(variant.getImages());
            vd.setIsDefault(variant.getIsDefault());

            Color color = colorMap.get(variant.getColorId());
            if (color != null) {
                vd.setColorName(color.getName());
                vd.setColorHexCode(color.getHexCode());
                vd.setColorSlug(color.getSlug());
            }

            // Lay danh sach stocks cua variant
            List<VariantStock> stocks = stockRepository.findByVariantId(variant.getId());
            List<Integer> sizeIds = stocks.stream().map(VariantStock::getSizeId).distinct().collect(Collectors.toList());
            Map<Integer, Size> sizeMap = sizeRepository.findAllById(sizeIds)
                    .stream().collect(Collectors.toMap(Size::getId, s -> s));

            List<ProductDetailResponse.StockDetail> stockDetails = new ArrayList<>();
            for (VariantStock stock : stocks) {
                ProductDetailResponse.StockDetail sd = new ProductDetailResponse.StockDetail();
                sd.setId(stock.getId());
                sd.setSizeId(stock.getSizeId());
                sd.setStockQty(stock.getStockQty());
                sd.setPriceOverride(stock.getPriceOverride());
                sd.setSku(stock.getSku());
                // Tinh gia thuc te
                BigDecimal effectivePrice = stock.getPriceOverride() != null
                        ? stock.getPriceOverride()
                        : product.getBasePrice();
                sd.setEffectivePrice(effectivePrice);

                Size size = sizeMap.get(stock.getSizeId());
                if (size != null) {
                    sd.setSizeLabel(size.getLabel());
                    sd.setSizeType(size.getType());
                }
                stockDetails.add(sd);
            }
            vd.setStocks(stockDetails);
            variantDetails.add(vd);
        }

        response.setVariants(variantDetails);
        return response;
    }
}

