package com.utc.ec.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "variant_stocks")
public class VariantStock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "variant_id", nullable = false)
    private Integer variantId;

    @Column(name = "size_id", nullable = false)
    private Integer sizeId;

    @Column(name = "stock_qty", nullable = false)
    private Integer stockQty = 0;

    @Column(name = "price_override", precision = 12, scale = 2)
    private BigDecimal priceOverride;

    @Column(name = "sku", nullable = false, unique = true, length = 100)
    private String sku;
}

