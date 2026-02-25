package com.utc.ec.controller;

import com.utc.ec.dto.ApiResponse;
import com.utc.ec.dto.ProductCategoryDTO;
import com.utc.ec.service.ProductCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product-categories")
@RequiredArgsConstructor
@Tag(name = "Product Category", description = "API quản lý danh mục sản phẩm")
public class ProductCategoryController {
    private final ProductCategoryService service;
    private final MessageSource messageSource;

    @Operation(summary = "Tạo danh mục sản phẩm", description = "Tạo mới một danh mục sản phẩm")
    @PostMapping
    public ApiResponse<ProductCategoryDTO> create(@RequestBody ProductCategoryDTO dto) {
        String message = messageSource.getMessage("productCategory.create.success", null, LocaleContextHolder.getLocale());
        return ApiResponse.success(message, service.create(dto));
    }

    @Operation(summary = "Cập nhật danh mục sản phẩm", description = "Cập nhật thông tin danh mục sản phẩm theo ID")
    @PutMapping("/{id}")
    public ApiResponse<ProductCategoryDTO> update(
            @Parameter(description = "ID danh mục cần cập nhật") @PathVariable Integer id,
            @RequestBody ProductCategoryDTO dto) {
        String message = messageSource.getMessage("productCategory.update.success", null, LocaleContextHolder.getLocale());
        return ApiResponse.success(message, service.update(id, dto));
    }

    @Operation(summary = "Xóa danh mục sản phẩm", description = "Xóa danh mục sản phẩm theo ID")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @Parameter(description = "ID danh mục cần xóa") @PathVariable Integer id) {
        service.delete(id);
        String message = messageSource.getMessage("productCategory.delete.success", null, LocaleContextHolder.getLocale());
        return ApiResponse.success(message, null);
    }

    @Operation(summary = "Lấy danh mục theo ID", description = "Lấy thông tin chi tiết danh mục sản phẩm theo ID")
    @GetMapping("/{id}")
    public ApiResponse<ProductCategoryDTO> getById(
            @Parameter(description = "ID danh mục cần lấy") @PathVariable Integer id) {
        return ApiResponse.success(null, service.getById(id));
    }

    @Operation(summary = "Lấy tất cả danh mục", description = "Lấy danh sách tất cả danh mục sản phẩm")
    @GetMapping
    public ApiResponse<List<ProductCategoryDTO>> getAll() {
        return ApiResponse.success(null, service.getAll());
    }
}