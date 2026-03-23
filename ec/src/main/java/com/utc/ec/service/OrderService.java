package com.utc.ec.service;

import com.utc.ec.dto.OrderDetailDTO;
import com.utc.ec.dto.OrderRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderService {

    /**
     * Dat hang tu gio hang hien tai cua user.
     * Tu dong tru ton kho va xoa gio hang sau khi dat thanh cong.
     */
    OrderDetailDTO placeOrder(String username, OrderRequest request);

    /**
     * Lay danh sach don hang cua user hien tai, sap xep moi nhat truoc.
     */
    List<OrderDetailDTO> getMyOrders(String username);

    /**
     * Xem chi tiet 1 don hang thuoc ve user hien tai.
     */
    OrderDetailDTO getMyOrderById(String username, Integer orderId);

    /**
     * User huy don hang - chi duoc huy khi trang thai PENDING.
     * Khoi phuc lai ton kho sau khi huy.
     */
    OrderDetailDTO cancelOrder(String username, Integer orderId);

    // ──────────────────────────────────────────────────
    //  ADMIN operations
    // ──────────────────────────────────────────────────

    /**
     * [ADMIN] Lay tat ca don hang, co phan trang.
     */
    Page<OrderDetailDTO> getAllOrders(Pageable pageable);

    /**
     * [ADMIN] Lay don hang theo trang thai, co phan trang.
     */
    Page<OrderDetailDTO> getOrdersByStatus(Integer statusId, Pageable pageable);

    /**
     * [ADMIN] Xem bat ky don hang nao theo ID.
     */
    OrderDetailDTO getOrderById(Integer orderId);

    /**
     * [ADMIN] Cap nhat trang thai don hang.
     */
    OrderDetailDTO updateOrderStatus(Integer orderId, Integer statusId);
}

