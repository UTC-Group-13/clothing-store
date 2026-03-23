package com.utc.ec.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderRequest {

    @NotNull(message = "paymentMethodId khong duoc de trong")
    private Integer paymentMethodId;

    @NotNull(message = "shippingAddressId khong duoc de trong")
    private Integer shippingAddressId;

    @NotNull(message = "shippingMethodId khong duoc de trong")
    private Integer shippingMethodId;
}

