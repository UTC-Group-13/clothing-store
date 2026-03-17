package com.utc.ec.dto.auth;

import lombok.Data;

@Data
public class PasswordRequest {
    private Integer userId;
    private String username;
    private String oldPassword;
    private String newPassword;
    private String verifyPassword;
    private String phoneNumber;
    private String emailAddress;
}
