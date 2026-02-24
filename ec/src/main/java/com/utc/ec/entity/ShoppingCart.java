package com.utc.ec.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "shopping_cart")
public class ShoppingCart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id")
    private Integer userId;
}

