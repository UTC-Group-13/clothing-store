package com.utc.ec.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "product_item")
public class ProductItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "product_id") // FK -> product(id)
    private Integer productId;

    @Column(name = "sku", length = 20, unique = true)
    private String sku;

    @Column(name = "qty_in_stock")
    private Integer qtyInStock;

    @Column(name = "product_image", length = 1000)
    private String productImage;

    @Column(name = "price")
    private Integer price;
}
