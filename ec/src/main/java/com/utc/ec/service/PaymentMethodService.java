package com.utc.ec.service;

import com.utc.ec.dto.UserPaymentMethodDTO;

import java.util.List;

public interface PaymentMethodService {

    /**
     * Lay danh sach phuong thuc thanh toan cua user hien tai.
     */
    List<UserPaymentMethodDTO> getMyPaymentMethods(String username);

    /**
     * Them phuong thuc thanh toan moi cho user.
     */
    UserPaymentMethodDTO addPaymentMethod(String username, UserPaymentMethodDTO dto);

    /**
     * Cap nhat phuong thuc thanh toan.
     */
    UserPaymentMethodDTO updatePaymentMethod(String username, Integer id, UserPaymentMethodDTO dto);

    /**
     * Xoa phuong thuc thanh toan.
     */
    void deletePaymentMethod(String username, Integer id);

    /**
     * Dat lam phuong thuc thanh toan mac dinh.
     */
    UserPaymentMethodDTO setDefault(String username, Integer id);
}

