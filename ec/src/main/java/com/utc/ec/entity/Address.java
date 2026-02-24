package com.utc.ec.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "address")
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "unit_number", length = 20)
    private String unitNumber;

    @Column(name = "street_number", length = 20)
    private String streetNumber;

    @Column(name = "address_line1", length = 500)
    private String addressLine1;

    @Column(name = "address_line2", length = 500)
    private String addressLine2;

    @Column(name = "city", length = 200)
    private String city;

    @Column(name = "region", length = 200)
    private String region;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "country_id") // FK -> country(id)
    private Integer countryId;
}
