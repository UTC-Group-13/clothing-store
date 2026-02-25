package com.utc.ec.controller;

import com.utc.ec.dto.ApiResponse;
import com.utc.ec.dto.ProductDTO;
import com.utc.ec.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Product", description = "API quản lý sản phẩm")
public class ProductController {
    private final ProductService service;
    private final MessageSource messageSource;

    @Operation(summary = "Tạo sản phẩm", description = "Tạo mới một sản phẩm")
    @PostMapping
    public ApiResponse<ProductDTO> create(@RequestBody ProductDTO dto) {
        return ApiResponse.<ProductDTO>builder()
                .success(true)
                .message(messageSource.getMessage("product.create.success", null, LocaleContextHolder.getLocale()))
                .data(service.create(dto))
                .build();
    }

    @Operation(summary = "Cập nhật sản phẩm", description = "Cập nhật thông tin sản phẩm theo ID")
    @PutMapping("/{id}")
    public ApiResponse<ProductDTO> update(
            @Parameter(description = "ID sản phẩm cần cập nhật") @PathVariable Integer id,
            @RequestBody ProductDTO dto) {
        return ApiResponse.<ProductDTO>builder()
                .success(true)
                .message(messageSource.getMessage("product.update.success", null, LocaleContextHolder.getLocale()))
                .data(service.update(id, dto))
                .build();
    }

    @Operation(summary = "Xóa sản phẩm", description = "Xóa sản phẩm theo ID")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @Parameter(description = "ID sản phẩm cần xóa") @PathVariable Integer id) {
        service.delete(id);
        return ApiResponse.<Void>builder()
                .success(true)
                .message(messageSource.getMessage("product.delete.success", null, LocaleContextHolder.getLocale()))
                .build();
    }

    @Operation(summary = "Lấy sản phẩm theo ID", description = "Lấy thông tin chi tiết sản phẩm theo ID")
    @GetMapping("/{id}")
    public ApiResponse<ProductDTO> getById(
            @Parameter(description = "ID sản phẩm cần lấy") @PathVariable Integer id) {
        return ApiResponse.<ProductDTO>builder()
                .success(true)
                .data(service.getById(id))
                .build();
    }

    @Operation(summary = "Lấy tất cả sản phẩm", description = "Lấy danh sách tất cả sản phẩm")
    @GetMapping
    public ApiResponse<List<ProductDTO>> getAll() {
        return ApiResponse.<List<ProductDTO>>builder()
                .success(true)
                .data(service.getAll())
                .build();
    }
}