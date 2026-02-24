package com.utc.ec.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "shipping_method")
public class ShippingMethod {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "price")
    private Integer price;
}

