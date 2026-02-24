package com.utc.ec.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "user_address")
@IdClass(UserAddressId.class)
public class UserAddress {
    @Id
    @Column(name = "user_id") // FK -> site_user(id)
    private Integer userId;

    @Id
    @Column(name = "address_id") // FK -> address(id)
    private Integer addressId;

    @Column(name = "is_default")
    private Integer isDefault;
}
