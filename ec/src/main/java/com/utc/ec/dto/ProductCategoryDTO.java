package com.utc.ec.dto;

import lombok.Data;

@Data
public class ProductCategoryDTO {
    private Integer id;
    private Integer parentCategoryId;
    private String categoryName;
}

