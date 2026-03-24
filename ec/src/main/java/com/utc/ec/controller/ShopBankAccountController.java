package com.utc.ec.controller;

import com.utc.ec.dto.ApiResponse;
import com.utc.ec.dto.ShopBankAccountDTO;
import com.utc.ec.service.ShopBankAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shop-bank-accounts")
@RequiredArgsConstructor
@Tag(name = "Shop Bank Account", description = "API quản lý tài khoản ngân hàng của shop (ADMIN cấu hình, User xem để chuyển khoản)")
public class ShopBankAccountController {

    private final ShopBankAccountService service;

    @Operation(summary = "Lấy tài khoản ngân hàng đang active",
               description = "Public - User xem để biết chuyển khoản vào đâu.")
    @GetMapping("/active")
    public ApiResponse<ShopBankAccountDTO> getActive() {
        return ApiResponse.success(null, service.getActive());
    }

    @Operation(summary = "[ADMIN] Lấy tất cả tài khoản ngân hàng")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ApiResponse<List<ShopBankAccountDTO>> getAll() {
        return ApiResponse.success(null, service.getAll());
    }

    @Operation(summary = "[ADMIN] Thêm tài khoản ngân hàng")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ShopBankAccountDTO> create(@Valid @RequestBody ShopBankAccountDTO dto) {
        return ApiResponse.success("Thêm tài khoản ngân hàng thành công", service.create(dto));
    }

    @Operation(summary = "[ADMIN] Cập nhật tài khoản ngân hàng")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ApiResponse<ShopBankAccountDTO> update(
            @Parameter(description = "ID tài khoản") @PathVariable Integer id,
            @Valid @RequestBody ShopBankAccountDTO dto) {
        return ApiResponse.success("Cập nhật tài khoản ngân hàng thành công", service.update(id, dto));
    }

    @Operation(summary = "[ADMIN] Xóa tài khoản ngân hàng")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@Parameter(description = "ID tài khoản") @PathVariable Integer id) {
        service.delete(id);
        return ApiResponse.success("Xóa tài khoản ngân hàng thành công", null);
    }

    @Operation(summary = "[ADMIN] Đặt tài khoản ngân hàng active",
               description = "Chỉ 1 tài khoản active tại 1 thời điểm. Khi set active cái mới, cái cũ tự động bị bỏ active.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/active")
    public ApiResponse<ShopBankAccountDTO> setActive(
            @Parameter(description = "ID tài khoản") @PathVariable Integer id) {
        return ApiResponse.success("Đã đặt tài khoản ngân hàng active", service.setActive(id));
    }
}

