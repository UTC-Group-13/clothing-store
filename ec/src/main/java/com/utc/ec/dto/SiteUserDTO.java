package com.utc.ec.dto;

import lombok.Data;

@Data
public class SiteUserDTO {
    private Integer id;
    private String emailAddress;
    private String phoneNumber;
    private String password;
}

