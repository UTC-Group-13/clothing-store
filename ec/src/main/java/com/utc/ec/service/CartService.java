package com.utc.ec.service;

import com.utc.ec.dto.CartItemRequest;
import com.utc.ec.dto.CartSummaryDTO;

public interface CartService {

    /**
     * Lay gio hang cua nguoi dung hien tai (tu dong tao neu chua co).
     */
    CartSummaryDTO getMyCart(String username);

    /**
     * Them san pham vao gio hang.
     * Neu san pham da co trong gio thi cong them so luong.
     */
    CartSummaryDTO addItem(String username, CartItemRequest request);

    /**
     * Cap nhat so luong cua 1 item trong gio.
     */
    CartSummaryDTO updateItem(String username, Integer itemId, CartItemRequest request);

    /**
     * Xoa 1 item khoi gio hang.
     */
    CartSummaryDTO removeItem(String username, Integer itemId);

    /**
     * Xoa toan bo item trong gio hang.
     */
    void clearCart(String username);
}

