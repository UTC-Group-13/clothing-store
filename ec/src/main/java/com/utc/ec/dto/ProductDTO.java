package com.utc.ec.dto;

import lombok.Data;

@Data
public class ProductDTO {
    private Integer id;
    private Integer categoryId;
    private String name;
    private String description;
    private String productImage;
}
