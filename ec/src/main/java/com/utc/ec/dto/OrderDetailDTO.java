package com.utc.ec.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDetailDTO {

    private Integer id;
    private Integer userId;
    private LocalDateTime orderDate;

    // Trang thai don hang
    private Integer statusId;
    private String statusName;

    // Phuong thuc thanh toan
    private Integer paymentMethodId;

    // Phuong thuc van chuyen
    private Integer shippingMethodId;
    private String shippingMethodName;
    private Integer shippingFee;

    // Dia chi giao hang
    private Integer shippingAddressId;
    private AddressDTO shippingAddressDetail;

    // Tong tien
    private Integer subtotal;
    private Integer orderTotal;

    // Chi tiet san pham
    private List<OrderLineDetailDTO> items;
}

