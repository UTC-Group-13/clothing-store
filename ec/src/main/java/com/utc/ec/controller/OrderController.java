package com.utc.ec.controller;

import com.utc.ec.dto.*;
import com.utc.ec.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Order", description = "API quan ly don hang")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;

    // ─────────────────────────────────────────────────────────────
    //  USER endpoints
    // ─────────────────────────────────────────────────────────────

    @Operation(
            summary = "Dat hang",
            description = "Tao don hang tu gio hang hien tai. " +
                          "Tu dong tru ton kho va lam trong gio hang sau khi dat thanh cong. " +
                          "Can co dia chi giao hang, phuong thuc van chuyen va phuong thuc thanh toan."
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<OrderDetailDTO> placeOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody OrderRequest request) {
        return ApiResponse.success("Dat hang thanh cong",
                orderService.placeOrder(userDetails.getUsername(), request));
    }

    @Operation(
            summary = "Lich su don hang",
            description = "Lay danh sach tat ca don hang cua ban than, sap xep moi nhat truoc."
    )
    @GetMapping
    public ApiResponse<List<OrderDetailDTO>> getMyOrders(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ApiResponse.success(null, orderService.getMyOrders(userDetails.getUsername()));
    }

    @Operation(
            summary = "Chi tiet 1 don hang",
            description = "Xem chi tiet don hang theo ID. Chi xem duoc don hang cua chinh minh."
    )
    @GetMapping("/{orderId}")
    public ApiResponse<OrderDetailDTO> getMyOrderById(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "ID don hang") @PathVariable Integer orderId) {
        return ApiResponse.success(null,
                orderService.getMyOrderById(userDetails.getUsername(), orderId));
    }

    @Operation(
            summary = "Huy don hang",
            description = "Chi duoc huy khi trang thai la PENDING. " +
                          "Tu dong hoan tra ton kho sau khi huy."
    )
    @PatchMapping("/{orderId}/cancel")
    public ApiResponse<OrderDetailDTO> cancelOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "ID don hang") @PathVariable Integer orderId) {
        return ApiResponse.success("Huy don hang thanh cong",
                orderService.cancelOrder(userDetails.getUsername(), orderId));
    }

    // ─────────────────────────────────────────────────────────────
    //  ADMIN endpoints
    // ─────────────────────────────────────────────────────────────

    @Operation(
            summary = "[ADMIN] Lay tat ca don hang",
            description = "Phan trang toan bo don hang trong he thong."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/all")
    public ApiResponse<PagedResponse<OrderDetailDTO>> getAllOrders(
            @Parameter(description = "So trang (tu 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "So luong moi trang") @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderDetailDTO> result = orderService.getAllOrders(pageable);
        return ApiResponse.success(null, buildPagedResponse(result));
    }

    @Operation(
            summary = "[ADMIN] Lay don hang theo trang thai",
            description = "Loc don hang theo statusId co phan trang."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/by-status/{statusId}")
    public ApiResponse<PagedResponse<OrderDetailDTO>> getOrdersByStatus(
            @Parameter(description = "ID trang thai") @PathVariable Integer statusId,
            @Parameter(description = "So trang (tu 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "So luong moi trang") @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderDetailDTO> result = orderService.getOrdersByStatus(statusId, pageable);
        return ApiResponse.success(null, buildPagedResponse(result));
    }

    @Operation(
            summary = "[ADMIN] Xem bat ky don hang",
            description = "Admin co the xem chi tiet don hang cua bat ky user nao."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/{orderId}")
    public ApiResponse<OrderDetailDTO> getOrderById(
            @Parameter(description = "ID don hang") @PathVariable Integer orderId) {
        return ApiResponse.success(null, orderService.getOrderById(orderId));
    }

    @Operation(
            summary = "[ADMIN] Cap nhat trang thai don hang",
            description = "Thay doi trang thai don hang: PENDING → PROCESSING → SHIPPED → DELIVERED hoac CANCELLED."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/admin/{orderId}/status")
    public ApiResponse<OrderDetailDTO> updateOrderStatus(
            @Parameter(description = "ID don hang") @PathVariable Integer orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ApiResponse.success("Cap nhat trang thai don hang thanh cong",
                orderService.updateOrderStatus(orderId, request.getStatusId()));
    }

    // ─────────────────────────────────────────────────────────────
    //  Helper
    // ─────────────────────────────────────────────────────────────

    private PagedResponse<OrderDetailDTO> buildPagedResponse(Page<OrderDetailDTO> page) {
        return PagedResponse.<OrderDetailDTO>builder()
                .content(page.getContent())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}

