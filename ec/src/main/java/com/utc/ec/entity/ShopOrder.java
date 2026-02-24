package com.utc.ec.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "shop_order")
public class ShopOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "user_id") // FK -> site_user(id)
    private Integer userId;

    @Column(name = "order_date")
    private LocalDateTime orderDate;

    @Column(name = "payment_method_id") // FK -> user_payment_method(id)
    private Integer paymentMethodId;

    @Column(name = "shipping_address") // FK -> address(id)
    private Integer shippingAddress;

    @Column(name = "shipping_method") // FK -> shipping_method(id)
    private Integer shippingMethod;

    @Column(name = "order_total")
    private Integer orderTotal;

    @Column(name = "order_status") // FK -> order_status(id)
    private Integer orderStatus;
}

