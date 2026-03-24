package com.utc.ec.service;

import com.utc.ec.dto.AddressDTO;

import java.util.List;

public interface UserAddressService {

    /** Lấy danh sách địa chỉ của user hiện tại */
    List<AddressDTO> getMyAddresses(String username);

    /** Thêm địa chỉ mới cho user */
    AddressDTO addAddress(String username, AddressDTO dto);

    /** Cập nhật địa chỉ */
    AddressDTO updateAddress(String username, Integer addressId, AddressDTO dto);

    /** Xóa địa chỉ */
    void deleteAddress(String username, Integer addressId);

    /** Đặt địa chỉ mặc định */
    AddressDTO setDefault(String username, Integer addressId);
}

