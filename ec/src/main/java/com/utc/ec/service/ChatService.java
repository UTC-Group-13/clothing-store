package com.utc.ec.service;

import com.utc.ec.dto.chat.ChatRequest;
import com.utc.ec.dto.chat.ChatResponse;

public interface ChatService {

    /**
     * Xử lý tin nhắn người dùng và trả về phản hồi từ AI kèm gợi ý sản phẩm.
     *
     * @param request  yêu cầu chat (message + sessionId tùy chọn)
     * @param username tên đăng nhập của user hiện tại (null nếu chưa đăng nhập)
     * @return phản hồi của AI + danh sách sản phẩm gợi ý
     */
    ChatResponse chat(ChatRequest request, String username);

    /**
     * Xóa session chat theo ID.
     *
     * @param sessionId ID của session cần xóa
     */
    void clearSession(String sessionId);
}

