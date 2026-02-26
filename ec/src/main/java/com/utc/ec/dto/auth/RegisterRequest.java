package com.utc.ec.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Thông tin đăng ký tài khoản")
public class RegisterRequest {

    @NotBlank(message = "Username không được để trống")
    @Size(min = 3, max = 50, message = "Username phải từ 3 đến 50 ký tự")
    @Schema(description = "Tên đăng nhập", example = "john_doe")
    private String username;

    @Email(message = "Email không hợp lệ")
    @Schema(description = "Địa chỉ email", example = "john@example.com")
    private String emailAddress;

    @Schema(description = "Số điện thoại", example = "0901234567")
    private String phoneNumber;

    @NotBlank(message = "Password không được để trống")
    @Size(min = 6, message = "Password phải có ít nhất 6 ký tự")
    @Schema(description = "Mật khẩu", example = "password123")
    private String password;
}

