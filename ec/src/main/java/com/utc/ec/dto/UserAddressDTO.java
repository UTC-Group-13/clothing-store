package com.utc.ec.dto;

import lombok.Data;

@Data
public class UserAddressDTO {
    private Integer userId;
    private Integer addressId;
    private Integer isDefault;
}

