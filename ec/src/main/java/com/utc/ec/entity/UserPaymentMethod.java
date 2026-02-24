package com.utc.ec.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "user_payment_method")
public class UserPaymentMethod {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "payment_type_id")
    private Integer paymentTypeId;

    private String provider;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "is_default")
    private Integer isDefault;
}

