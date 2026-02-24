package com.utc.ec.entity;

import java.io.Serializable;
import lombok.Data;

@Data
public class PromotionCategoryId implements Serializable {
    private Integer categoryId;
    private Integer promotionId;
}

