package com.utc.ec.service;

import com.utc.ec.dto.OrderDetailDTO;

public interface EmailService {

    /**
     * Gửi email xác nhận đặt hàng thành công (async).
     *
     * @param toEmail      Địa chỉ email người nhận
     * @param customerName Tên hiển thị trong email
     * @param order        Chi tiết đơn hàng
     */
    void sendOrderConfirmationEmail(String toEmail, String customerName, OrderDetailDTO order);
}

