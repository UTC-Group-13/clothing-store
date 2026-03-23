package com.utc.ec.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemDetailDTO {

    private Integer id;
    private Integer cartId;
    private Integer variantStockId;
    private String sku;
    private Integer qty;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    private Integer availableStock;

    // Product info
    private Integer productId;
    private String productName;
    private String productSlug;

    // Variant info
    private Integer variantId;
    private String colorName;
    private String colorHex;
    private String colorImageUrl;

    // Size info
    private String sizeLabel;
    private String sizeType;
}

