package com.utc.ec.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserPaymentMethodDTO {
    private Integer id;
    private Integer userId;

    @NotNull(message = "paymentTypeId khong duoc de trong")
    private Integer paymentTypeId;

    private String provider;
    private String accountNumber;
    private LocalDate expiryDate;
    private Integer isDefault;
}
