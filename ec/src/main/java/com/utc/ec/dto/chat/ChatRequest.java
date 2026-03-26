package com.utc.ec.dto.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Yêu cầu chat với AI Shopping Assistant")
public class ChatRequest {

    @NotBlank(message = "Tin nhan khong duoc trong")
    @Size(max = 1000, message = "Tin nhan toi da 1000 ky tu")
    @Schema(description = "Tin nhắn của người dùng", example = "Tôi muốn mua áo thun màu đỏ dưới 300k")
    private String message;

    @Schema(
        description = "Session ID để duy trì ngữ cảnh hội thoại (null = tạo phiên mới)",
        example = "550e8400-e29b-41d4-a716-446655440000"
    )
    private String sessionId;

    @Schema(description = "ID sản phẩm đang xem (tuỳ chọn, giúp AI hiểu ngữ cảnh)", example = "5")
    private Integer productId;
}

