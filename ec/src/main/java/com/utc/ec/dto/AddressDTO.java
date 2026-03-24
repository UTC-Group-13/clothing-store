package com.utc.ec.dto;

import lombok.Data;

@Data
public class AddressDTO {
    private Integer id;
    private String unitNumber;
    private String streetNumber;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String region;
    private String postalCode;
    private Integer countryId;
    private Boolean isDefault;
}

