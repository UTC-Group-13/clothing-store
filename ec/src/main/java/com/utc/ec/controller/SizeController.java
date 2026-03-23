package com.utc.ec.controller;

import com.utc.ec.dto.ApiResponse;
import com.utc.ec.dto.SizeDTO;
import com.utc.ec.service.SizeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sizes")
@RequiredArgsConstructor
@Tag(name = "Size", description = "API quản lý size sản phẩm")
public class SizeController {

    private final SizeService service;
    private final MessageSource messageSource;

    @Operation(summary = "Tạo size")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SizeDTO> create(@Valid @RequestBody SizeDTO dto) {
        return ApiResponse.success(
                messageSource.getMessage("size.create.success", null, LocaleContextHolder.getLocale()),
                service.create(dto));
    }

    @Operation(summary = "Cập nhật size")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    public ApiResponse<SizeDTO> update(
            @Parameter(description = "ID size") @PathVariable Integer id,
            @Valid @RequestBody SizeDTO dto) {
        return ApiResponse.success(
                messageSource.getMessage("size.update.success", null, LocaleContextHolder.getLocale()),
                service.update(id, dto));
    }

    @Operation(summary = "Xóa size", description = "Không xóa được nếu đang có tồn kho sử dụng.")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@Parameter(description = "ID size") @PathVariable Integer id) {
        service.delete(id);
        return ApiResponse.success(
                messageSource.getMessage("size.delete.success", null, LocaleContextHolder.getLocale()),
                null);
    }

    @Operation(summary = "Lấy size theo ID")
    @GetMapping("/{id}")
    public ApiResponse<SizeDTO> getById(@Parameter(description = "ID size") @PathVariable Integer id) {
        return ApiResponse.success(null, service.getById(id));
    }

    @Operation(summary = "Lấy tất cả sizes")
    @GetMapping
    public ApiResponse<List<SizeDTO>> getAll() {
        return ApiResponse.success(null, service.getAll());
    }

    @Operation(summary = "Lấy sizes theo loại", description = "Lấy sizes theo type: clothing, numeric, shoes")
    @GetMapping("/type/{type}")
    public ApiResponse<List<SizeDTO>> getByType(
            @Parameter(description = "Loại size: clothing | numeric | shoes") @PathVariable String type) {
        return ApiResponse.success(null, service.getByType(type));
    }
}

