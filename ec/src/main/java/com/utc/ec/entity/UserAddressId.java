package com.utc.ec.entity;

import java.io.Serializable;
import lombok.Data;

@Data
public class UserAddressId implements Serializable {
    private Integer userId;
    private Integer addressId;
}

