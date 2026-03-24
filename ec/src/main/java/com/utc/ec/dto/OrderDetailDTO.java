package com.utc.ec.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDetailDTO {

    private Integer id;
    private String orderCode;
    private Integer userId;
    private LocalDateTime orderDate;

    // Trang thai don hang
    private Integer statusId;
    private String statusName;

    // Thanh toan
    private Integer paymentTypeId;
    private String paymentTypeName;
    private String paymentNote;

    /** URL mã QR chuyển khoản (chỉ có khi paymentType = "Chuyển khoản ngân hàng") */
    private String qrUrl;

    /** Thông tin tài khoản ngân hàng shop (chỉ có khi chuyển khoản) */
    private ShopBankAccountDTO bankInfo;

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

