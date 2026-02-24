package com.utc.ec.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "order_line")
public class OrderLine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "product_item_id") // FK -> product_item(id)
    private Integer productItemId;

    @Column(name = "order_id") // FK -> shop_order(id)
    private Integer orderId;

    @Column(name = "qty")
    private Integer qty;

    @Column(name = "price")
    private Integer price;
}

