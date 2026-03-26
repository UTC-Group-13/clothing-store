package com.utc.ec.controller;

import com.utc.ec.dto.ApiResponse;
import com.utc.ec.dto.chat.ChatRequest;
import com.utc.ec.dto.chat.ChatResponse;
import com.utc.ec.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "Chat AI", description = "API chatbot AI gợi ý sản phẩm — không cần đăng nhập để sử dụng")
public class ChatController {

    private final ChatService chatService;

    @Operation(
        summary = "Gửi tin nhắn cho AI Shopping Assistant",
        description = """
            Gửi tin nhắn và nhận phản hồi từ AI kèm danh sách sản phẩm gợi ý.
            
            **Không cần đăng nhập** — tuy nhiên nếu đã đăng nhập, AI sẽ được cá nhân hóa hơn.
            
            **Cách dùng:**
            - Lần đầu: không cần gửi `sessionId`
            - Các lần sau: gửi `sessionId` từ response trước để AI nhớ ngữ cảnh
            - Session tự động hết hạn sau 2 giờ không hoạt động
            
            **Ví dụ tin nhắn:**
            - "Tôi muốn mua áo thun màu đỏ dưới 300k"
            - "Áo này có phối với quần jeans không?"
            - "Tôi cao 1m70 nặng 65kg mặc size gì?"
            - "Cho tôi xem áo khoác mùa đông"
            """
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        content = @Content(examples = {
            @ExampleObject(name = "Tìm sản phẩm theo ngân sách",
                value = """
                    {
                      "message": "Tôi muốn mua áo thun cotton dưới 300k, có màu đỏ không?",
                      "sessionId": null
                    }
                    """),
            @ExampleObject(name = "Hỏi về size",
                value = """
                    {
                      "message": "Tôi cao 1m70, nặng 65kg thì mặc size M hay L?",
                      "sessionId": "550e8400-e29b-41d4-a716-446655440000"
                    }
                    """),
            @ExampleObject(name = "Hỏi về sản phẩm đang xem",
                value = """
                    {
                      "message": "Áo này có phối được với quần jeans xanh không?",
                      "sessionId": "550e8400-e29b-41d4-a716-446655440000",
                      "productId": 5
                    }
                    """)
        })
    )
    @PostMapping("/message")
    public ApiResponse<ChatResponse> chat(
            @Valid @RequestBody ChatRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        String username = userDetails != null ? userDetails.getUsername() : null;
        ChatResponse response = chatService.chat(request, username);
        return ApiResponse.success("Phan hoi thanh cong", response);
    }

    @Operation(
        summary = "Xóa lịch sử chat",
        description = "Xóa toàn bộ lịch sử hội thoại của session. Dùng khi muốn bắt đầu lại từ đầu."
    )
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/session/{sessionId}")
    public ApiResponse<Void> clearSession(
            @Parameter(description = "Session ID cần xóa") @PathVariable String sessionId) {
        chatService.clearSession(sessionId);
        return ApiResponse.success("Xoa session thanh cong", null);
    }
}


