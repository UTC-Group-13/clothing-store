package com.utc.ec.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "product_category")
public class ProductCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "parent_category_id") // FK -> product_category(id)
    private Integer parentCategoryId;

    @Column(name = "category_name", length = 200)
    private String categoryName;
}
