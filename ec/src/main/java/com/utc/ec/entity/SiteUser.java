package com.utc.ec.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "site_user")
public class SiteUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "email_address", length = 350)
    private String emailAddress;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "password", length = 500)
    private String password;
}
