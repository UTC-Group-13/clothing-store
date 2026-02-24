package com.utc.ec.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "shopping_cart_item")
public class ShoppingCartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "cart_id")
    private Integer cartId;

    @Column(name = "product_item_id")
    private Integer productItemId;

    private Integer qty;
}

