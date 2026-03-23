package com.utc.ec.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "product_variants")
public class ProductVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "product_id", nullable = false)
    private Integer productId;

    @Column(name = "color_id", nullable = false)
    private Integer colorId;

    @Column(name = "color_image_url", length = 500)
    private String colorImageUrl;

    @Column(name = "images", columnDefinition = "JSON")
    private String images;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;
}

