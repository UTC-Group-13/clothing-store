package com.utc.ec.controller;

import com.utc.ec.dto.ApiResponse;
import com.utc.ec.dto.ProductVariantDTO;
import com.utc.ec.service.ProductVariantService;
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
@RequestMapping("/api/product-variants")
@RequiredArgsConstructor
@Tag(name = "Product Variant", description = "API quản lý biến thể sản phẩm (theo màu)")
public class ProductVariantController {

    private final ProductVariantService service;
    private final MessageSource messageSource;

    @Operation(summary = "Tạo biến thể", description = "Gắn một màu sắc vào sản phẩm. Mỗi cặp (product, color) là duy nhất.")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProductVariantDTO> create(@Valid @RequestBody ProductVariantDTO dto) {
        return ApiResponse.success(
                messageSource.getMessage("productVariant.create.success", null, LocaleContextHolder.getLocale()),
                service.create(dto));
    }

    @Operation(summary = "Cập nhật biến thể")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    public ApiResponse<ProductVariantDTO> update(
            @Parameter(description = "ID biến thể") @PathVariable Integer id,
            @Valid @RequestBody ProductVariantDTO dto) {
        return ApiResponse.success(
                messageSource.getMessage("productVariant.update.success", null, LocaleContextHolder.getLocale()),
                service.update(id, dto));
    }

    @Operation(summary = "Xóa biến thể", description = "Không xóa được nếu đang có tồn kho.")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@Parameter(description = "ID biến thể") @PathVariable Integer id) {
        service.delete(id);
        return ApiResponse.success(
                messageSource.getMessage("productVariant.delete.success", null, LocaleContextHolder.getLocale()),
                null);
    }

    @Operation(summary = "Lấy biến thể theo ID")
    @GetMapping("/{id}")
    public ApiResponse<ProductVariantDTO> getById(@Parameter(description = "ID biến thể") @PathVariable Integer id) {
        return ApiResponse.success(null, service.getById(id));
    }

    @Operation(summary = "Lấy tất cả biến thể")
    @GetMapping
    public ApiResponse<List<ProductVariantDTO>> getAll() {
        return ApiResponse.success(null, service.getAll());
    }

    @Operation(summary = "Lấy biến thể theo sản phẩm")
    @GetMapping("/product/{productId}")
    public ApiResponse<List<ProductVariantDTO>> getByProductId(
            @Parameter(description = "ID sản phẩm") @PathVariable Integer productId) {
        return ApiResponse.success(null, service.getByProductId(productId));
    }
}

