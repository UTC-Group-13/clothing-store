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

    @Column(name = "variant_stock_id")
    private Integer variantStockId;

    @Column(name = "order_id")
    private Integer orderId;

    @Column(name = "qty")
    private Integer qty;

    @Column(name = "price")
    private Integer price;
}
