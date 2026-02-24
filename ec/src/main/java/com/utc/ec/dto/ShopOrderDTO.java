package com.utc.ec.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ShopOrderDTO {
    private Integer id;
    private Integer userId;
    private LocalDateTime orderDate;
    private Integer paymentMethodId;
    private Integer shippingAddress;
    private Integer shippingMethod;
    private Integer orderTotal;
    private Integer orderStatus;
}

