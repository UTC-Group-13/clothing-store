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

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "order_date")
    private LocalDateTime orderDate;

    /** Mã đơn hàng duy nhất (VD: DH20260324001) */
    @Column(name = "order_code", unique = true, length = 30)
    private String orderCode;

    /** Loại thanh toán: COD, Chuyển khoản... (FK → payment_type) */
    @Column(name = "payment_type_id")
    private Integer paymentTypeId;

    /** Ghi chú thanh toán (VD: "Đã chuyển khoản lúc 10:30") */
    @Column(name = "payment_note", length = 500)
    private String paymentNote;

    @Column(name = "payment_method_id") // giữ lại cho đơn hàng cũ, nullable
    private Integer paymentMethodId;

    @Column(name = "shipping_address")
    private Integer shippingAddress;

    @Column(name = "shipping_method")
    private Integer shippingMethod;

    @Column(name = "order_total")
    private Integer orderTotal;

    @Column(name = "order_status")
    private Integer orderStatus;
}

