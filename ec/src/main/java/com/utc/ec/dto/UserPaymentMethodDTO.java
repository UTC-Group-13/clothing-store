package com.utc.ec.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class UserPaymentMethodDTO {
    private Integer id;
    private Integer userId;
    private Integer paymentTypeId;
    private String provider;
    private String accountNumber;
    private LocalDate expiryDate;
    private Integer isDefault;
}
