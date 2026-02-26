package com.utc.ec.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Kết quả xác thực")
public class AuthResponse {

    @Schema(description = "JWT access token")
    private String accessToken;

    @Schema(description = "Loại token", example = "Bearer")
    private String tokenType;

    @Schema(description = "ID người dùng")
    private Integer userId;

    @Schema(description = "Tên đăng nhập")
    private String username;

    @Schema(description = "Email")
    private String emailAddress;

    @Schema(description = "Vai trò", example = "USER")
    private String role;
}

