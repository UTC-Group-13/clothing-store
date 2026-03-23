package com.utc.ec.controller;

import com.utc.ec.dto.ApiResponse;
import com.utc.ec.dto.UserPaymentMethodDTO;
import com.utc.ec.service.PaymentMethodService;
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
@RequestMapping("/api/payment-methods")
@RequiredArgsConstructor
@Tag(name = "Payment Method", description = "API quan ly phuong thuc thanh toan cua nguoi dung")
@SecurityRequirement(name = "bearerAuth")
public class PaymentMethodController {

    private final PaymentMethodService paymentMethodService;

    @Operation(
            summary = "Lay danh sach phuong thuc thanh toan",
            description = "Tra ve tat ca phuong thuc thanh toan da luu cua nguoi dung hien tai."
    )
    @GetMapping
    public ApiResponse<List<UserPaymentMethodDTO>> getMyPaymentMethods(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ApiResponse.success(null,
                paymentMethodService.getMyPaymentMethods(userDetails.getUsername()));
    }

    @Operation(
            summary = "Them phuong thuc thanh toan",
            description = "Them phuong thuc thanh toan moi (Visa, MoMo, COD...). " +
                          "Neu la phuong thuc dau tien se tu dong dat lam mac dinh."
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserPaymentMethodDTO> addPaymentMethod(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserPaymentMethodDTO dto) {
        return ApiResponse.success("Them phuong thuc thanh toan thanh cong",
                paymentMethodService.addPaymentMethod(userDetails.getUsername(), dto));
    }

    @Operation(
            summary = "Cap nhat phuong thuc thanh toan",
            description = "Cap nhat thong tin (provider, account number, expiry date) cua 1 phuong thuc."
    )
    @PutMapping("/{id}")
    public ApiResponse<UserPaymentMethodDTO> updatePaymentMethod(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "ID phuong thuc thanh toan") @PathVariable Integer id,
            @Valid @RequestBody UserPaymentMethodDTO dto) {
        return ApiResponse.success("Cap nhat phuong thuc thanh toan thanh cong",
                paymentMethodService.updatePaymentMethod(userDetails.getUsername(), id, dto));
    }

    @Operation(
            summary = "Xoa phuong thuc thanh toan",
            description = "Khong xoa duoc neu phuong thuc dang duoc dung trong don hang."
    )
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletePaymentMethod(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "ID phuong thuc thanh toan") @PathVariable Integer id) {
        paymentMethodService.deletePaymentMethod(userDetails.getUsername(), id);
        return ApiResponse.success("Xoa phuong thuc thanh toan thanh cong", null);
    }

    @Operation(
            summary = "Dat lam phuong thuc thanh toan mac dinh",
            description = "Dat phuong thuc co id nay lam mac dinh, bo mac dinh cac phuong thuc khac."
    )
    @PatchMapping("/{id}/default")
    public ApiResponse<UserPaymentMethodDTO> setDefault(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "ID phuong thuc thanh toan") @PathVariable Integer id) {
        return ApiResponse.success("Da dat lam phuong thuc mac dinh",
                paymentMethodService.setDefault(userDetails.getUsername(), id));
    }
}

