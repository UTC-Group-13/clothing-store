package com.utc.ec.controller;

import com.utc.ec.dto.ApiResponse;
import com.utc.ec.dto.PaymentTypeDTO;
import com.utc.ec.service.PaymentTypeService;
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
@RequestMapping("/api/payment-types")
@RequiredArgsConstructor
@Tag(name = "Payment Type", description = "API danh muc loai thanh toan (COD, Visa, MoMo...)")
public class PaymentTypeController {

    private final PaymentTypeService paymentTypeService;

    @Operation(summary = "Lay danh sach loai thanh toan", description = "Public - khong can dang nhap.")
    @GetMapping
    public ApiResponse<List<PaymentTypeDTO>> getAll() {
        return ApiResponse.success(null, paymentTypeService.getAll());
    }

    @Operation(summary = "Lay loai thanh toan theo ID", description = "Public - khong can dang nhap.")
    @GetMapping("/{id}")
    public ApiResponse<PaymentTypeDTO> getById(
            @Parameter(description = "ID loai thanh toan") @PathVariable Integer id) {
        return ApiResponse.success(null, paymentTypeService.getById(id));
    }

    @Operation(summary = "Tao loai thanh toan moi (ADMIN)", description = "Chi ADMIN moi co quyen tao.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PaymentTypeDTO> create(@Valid @RequestBody PaymentTypeDTO dto) {
        return ApiResponse.success("Tao loai thanh toan thanh cong", paymentTypeService.create(dto));
    }

    @Operation(summary = "Cap nhat loai thanh toan (ADMIN)", description = "Chi ADMIN moi co quyen cap nhat.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ApiResponse<PaymentTypeDTO> update(
            @Parameter(description = "ID loai thanh toan") @PathVariable Integer id,
            @Valid @RequestBody PaymentTypeDTO dto) {
        return ApiResponse.success("Cap nhat loai thanh toan thanh cong", paymentTypeService.update(id, dto));
    }

    @Operation(summary = "Xoa loai thanh toan (ADMIN)",
               description = "Chi ADMIN moi co quyen xoa. Khong xoa duoc neu dang duoc su dung.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @Parameter(description = "ID loai thanh toan") @PathVariable Integer id) {
        paymentTypeService.delete(id);
        return ApiResponse.success("Xoa loai thanh toan thanh cong", null);
    }
}

