package com.utc.ec.dto;

import lombok.Data;

@Data
public class ProductItemDTO {
    private Integer id;
    private Integer productId;
    private String sku;
    private Integer qtyInStock;
    private String productImage;
    private Integer price;
}

