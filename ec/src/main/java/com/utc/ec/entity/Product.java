package com.utc.ec.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "category_id") // FK -> product_category(id)
    private Integer categoryId;

    @Column(name = "name", length = 500)
    private String name;

    @Column(name = "description", length = 4000)
    private String description;

    @Column(name = "product_image", length = 1000)
    private String productImage;
}
