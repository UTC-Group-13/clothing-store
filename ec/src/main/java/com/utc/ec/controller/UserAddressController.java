package com.utc.ec.controller;

import com.utc.ec.dto.AddressDTO;
import com.utc.ec.dto.ApiResponse;
import com.utc.ec.service.UserAddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
@Tag(name = "User Address", description = "API quản lý địa chỉ giao hàng của user")
@SecurityRequirement(name = "bearerAuth")
public class UserAddressController {

    private final UserAddressService service;

    @Operation(summary = "Lấy danh sách địa chỉ của tôi")
    @GetMapping
    public ApiResponse<List<AddressDTO>> getMyAddresses(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ApiResponse.success(null, service.getMyAddresses(userDetails.getUsername()));
    }

    @Operation(summary = "Thêm địa chỉ mới",
               description = "Địa chỉ đầu tiên tự động được đặt làm mặc định.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AddressDTO> addAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AddressDTO dto) {
        return ApiResponse.success("Thêm địa chỉ thành công",
                service.addAddress(userDetails.getUsername(), dto));
    }

    @Operation(summary = "Cập nhật địa chỉ")
    @PutMapping("/{addressId}")
    public ApiResponse<AddressDTO> updateAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "ID địa chỉ") @PathVariable Integer addressId,
            @Valid @RequestBody AddressDTO dto) {
        return ApiResponse.success("Cập nhật địa chỉ thành công",
                service.updateAddress(userDetails.getUsername(), addressId, dto));
    }

    @Operation(summary = "Xóa địa chỉ")
    @DeleteMapping("/{addressId}")
    public ApiResponse<Void> deleteAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "ID địa chỉ") @PathVariable Integer addressId) {
        service.deleteAddress(userDetails.getUsername(), addressId);
        return ApiResponse.success("Xóa địa chỉ thành công", null);
    }

    @Operation(summary = "Đặt địa chỉ mặc định",
               description = "Chỉ 1 địa chỉ mặc định. Cái cũ tự động bỏ mặc định.")
    @PatchMapping("/{addressId}/default")
    public ApiResponse<AddressDTO> setDefault(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "ID địa chỉ") @PathVariable Integer addressId) {
        return ApiResponse.success("Đã đặt địa chỉ mặc định",
                service.setDefault(userDetails.getUsername(), addressId));
    }
}

