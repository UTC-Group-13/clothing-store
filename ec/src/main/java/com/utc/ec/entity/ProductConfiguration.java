package com.utc.ec.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "product_configuration")
@IdClass(ProductConfigurationId.class)
public class ProductConfiguration {
    @Id
    @Column(name = "product_item_id")
    private Integer productItemId;

    @Id
    @Column(name = "variation_option_id")
    private Integer variationOptionId;
}

