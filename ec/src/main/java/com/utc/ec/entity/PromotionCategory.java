package com.utc.ec.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "promotion_category")
@IdClass(PromotionCategoryId.class)
public class PromotionCategory {
    @Id
    @Column(name = "category_id") // FK -> product_category(id)
    private Integer categoryId;

    @Id
    @Column(name = "promotion_id") // FK -> promotion(id)
    private Integer promotionId;
}
