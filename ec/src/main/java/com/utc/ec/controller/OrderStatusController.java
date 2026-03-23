package com.utc.ec.controller;

import com.utc.ec.dto.ApiResponse;
import com.utc.ec.dto.OrderStatusDTO;
import com.utc.ec.service.OrderStatusService;
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
@RequestMapping("/api/order-statuses")
@RequiredArgsConstructor
@Tag(name = "Order Status", description = "API quan ly trang thai don hang (PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED)")
public class OrderStatusController {

    private final OrderStatusService orderStatusService;

    @Operation(summary = "Lay danh sach trang thai don hang", description = "Public - khong can dang nhap.")
    @GetMapping
    public ApiResponse<List<OrderStatusDTO>> getAll() {
        return ApiResponse.success(null, orderStatusService.getAll());
    }

    @Operation(summary = "Lay trang thai don hang theo ID", description = "Public - khong can dang nhap.")
    @GetMapping("/{id}")
    public ApiResponse<OrderStatusDTO> getById(
            @Parameter(description = "ID trang thai") @PathVariable Integer id) {
        return ApiResponse.success(null, orderStatusService.getById(id));
    }

    @Operation(summary = "Tao trang thai don hang (ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<OrderStatusDTO> create(@Valid @RequestBody OrderStatusDTO dto) {
        return ApiResponse.success("Tao trang thai don hang thanh cong",
                orderStatusService.create(dto));
    }

    @Operation(summary = "Cap nhat trang thai don hang (ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ApiResponse<OrderStatusDTO> update(
            @Parameter(description = "ID trang thai") @PathVariable Integer id,
            @Valid @RequestBody OrderStatusDTO dto) {
        return ApiResponse.success("Cap nhat trang thai don hang thanh cong",
                orderStatusService.update(id, dto));
    }

    @Operation(summary = "Xoa trang thai don hang (ADMIN)",
               description = "Khong xoa duoc neu dang duoc su dung boi don hang.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @Parameter(description = "ID trang thai") @PathVariable Integer id) {
        orderStatusService.delete(id);
        return ApiResponse.success("Xoa trang thai don hang thanh cong", null);
    }
}

