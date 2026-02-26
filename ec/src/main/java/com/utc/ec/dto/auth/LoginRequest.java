package com.utc.ec.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Thông tin đăng nhập")
public class LoginRequest {

    @NotBlank(message = "Username không được để trống")
    @Schema(description = "Tên đăng nhập", example = "john_doe")
    private String username;

    @NotBlank(message = "Password không được để trống")
    @Schema(description = "Mật khẩu", example = "password123")
    private String password;
}

