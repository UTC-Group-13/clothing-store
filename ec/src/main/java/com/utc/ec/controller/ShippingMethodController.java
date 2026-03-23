package com.utc.ec.controller;

import com.utc.ec.dto.ApiResponse;
import com.utc.ec.dto.ShippingMethodDTO;
import com.utc.ec.service.ShippingMethodService;
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
@RequestMapping("/api/shipping-methods")
@RequiredArgsConstructor
@Tag(name = "Shipping Method", description = "API quan ly phuong thuc van chuyen")
public class ShippingMethodController {

    private final ShippingMethodService shippingMethodService;

    @Operation(summary = "Lay danh sach phuong thuc van chuyen", description = "Public - khong can dang nhap.")
    @GetMapping
    public ApiResponse<List<ShippingMethodDTO>> getAll() {
        return ApiResponse.success(null, shippingMethodService.getAll());
    }

    @Operation(summary = "Lay phuong thuc van chuyen theo ID", description = "Public - khong can dang nhap.")
    @GetMapping("/{id}")
    public ApiResponse<ShippingMethodDTO> getById(
            @Parameter(description = "ID phuong thuc van chuyen") @PathVariable Integer id) {
        return ApiResponse.success(null, shippingMethodService.getById(id));
    }

    @Operation(summary = "Tao phuong thuc van chuyen (ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ShippingMethodDTO> create(@Valid @RequestBody ShippingMethodDTO dto) {
        return ApiResponse.success("Tao phuong thuc van chuyen thanh cong",
                shippingMethodService.create(dto));
    }

    @Operation(summary = "Cap nhat phuong thuc van chuyen (ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ApiResponse<ShippingMethodDTO> update(
            @Parameter(description = "ID phuong thuc van chuyen") @PathVariable Integer id,
            @Valid @RequestBody ShippingMethodDTO dto) {
        return ApiResponse.success("Cap nhat phuong thuc van chuyen thanh cong",
                shippingMethodService.update(id, dto));
    }

    @Operation(summary = "Xoa phuong thuc van chuyen (ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @Parameter(description = "ID phuong thuc van chuyen") @PathVariable Integer id) {
        shippingMethodService.delete(id);
        return ApiResponse.success("Xoa phuong thuc van chuyen thanh cong", null);
    }
}

