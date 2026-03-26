package com.utc.ec.dto.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@Schema(description = "Phản hồi từ chatbot AI")
public class ChatResponse {

    @Schema(description = "Nội dung phản hồi của AI")
    private String message;

    @Schema(
        description = "Session ID để gửi cho request tiếp theo (giữ ngữ cảnh hội thoại)",
        example = "550e8400-e29b-41d4-a716-446655440000"
    )
    private String sessionId;

    @Schema(description = "Danh sách sản phẩm được AI gợi ý (tối đa 5 sản phẩm)")
    private List<ProductSuggestionDTO> suggestions;

    @Builder.Default
    private Instant timestamp = Instant.now();
}

