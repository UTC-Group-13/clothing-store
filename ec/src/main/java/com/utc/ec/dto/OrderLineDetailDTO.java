package com.utc.ec.dto;

import lombok.Data;

@Data
public class OrderLineDetailDTO {

    private Integer id;
    private Integer variantStockId;
    private String sku;
    private Integer qty;
    private Integer price;
    private Integer subtotal;

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

