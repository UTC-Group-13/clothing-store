package com.utc.ec.controller;

import com.utc.ec.dto.ApiResponse;
import com.utc.ec.dto.CartItemRequest;
import com.utc.ec.dto.CartSummaryDTO;
import com.utc.ec.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Shopping Cart", description = "API quan ly gio hang")
@SecurityRequirement(name = "bearerAuth")
public class CartController {

    private final CartService cartService;

    @Operation(
            summary = "Xem gio hang",
            description = "Lay toan bo thong tin gio hang cua nguoi dung hien tai kem chi tiet san pham, mau, size va tong tien."
    )
    @GetMapping
    public ApiResponse<CartSummaryDTO> getMyCart(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ApiResponse.success(null, cartService.getMyCart(userDetails.getUsername()));
    }

    @Operation(
            summary = "Them san pham vao gio",
            description = "Them 1 san pham (variant_stock) vao gio. Neu san pham da co thi cong them so luong. " +
                          "Tra loi 400 neu vuot qua so luong ton kho."
    )
    @PostMapping("/items")
    public ApiResponse<CartSummaryDTO> addItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CartItemRequest request) {
        return ApiResponse.success("Them san pham vao gio thanh cong",
                cartService.addItem(userDetails.getUsername(), request));
    }

    @Operation(
            summary = "Cap nhat so luong item trong gio",
            description = "Thay doi so luong (qty) cua 1 item dang co trong gio hang. qty phai >= 1."
    )
    @PutMapping("/items/{itemId}")
    public ApiResponse<CartSummaryDTO> updateItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "ID item trong gio hang") @PathVariable Integer itemId,
            @Valid @RequestBody CartItemRequest request) {
        return ApiResponse.success("Cap nhat so luong thanh cong",
                cartService.updateItem(userDetails.getUsername(), itemId, request));
    }

    @Operation(
            summary = "Xoa 1 item khoi gio hang",
            description = "Xoa san pham co itemId ra khoi gio hang cua nguoi dung."
    )
    @DeleteMapping("/items/{itemId}")
    public ApiResponse<CartSummaryDTO> removeItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "ID item trong gio hang") @PathVariable Integer itemId) {
        return ApiResponse.success("Xoa san pham khoi gio thanh cong",
                cartService.removeItem(userDetails.getUsername(), itemId));
    }

    @Operation(
            summary = "Lam trong gio hang",
            description = "Xoa toan bo san pham trong gio hang."
    )
    @DeleteMapping
    public ApiResponse<Void> clearCart(
            @AuthenticationPrincipal UserDetails userDetails) {
        cartService.clearCart(userDetails.getUsername());
        return ApiResponse.success("Lam trong gio hang thanh cong", null);
    }
}

