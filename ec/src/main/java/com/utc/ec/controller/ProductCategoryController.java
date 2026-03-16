package com.utc.ec.controller;

import com.utc.ec.dto.ApiResponse;
import com.utc.ec.dto.ProductCategoryDTO;
import com.utc.ec.service.ProductCategoryService;
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
@RequestMapping("/api/product-categories")
@RequiredArgsConstructor
@Tag(name = "Product Category", description = "API quản lý danh mục sản phẩm")
public class ProductCategoryController {

    private final ProductCategoryService service;
    private final MessageSource messageSource;

    @Operation(summary = "Tạo danh mục sản phẩm", description = "Tạo mới một danh mục. parentCategoryId = null nếu là danh mục gốc.")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProductCategoryDTO> create(@Valid @RequestBody ProductCategoryDTO dto) {
        return ApiResponse.success(
                messageSource.getMessage("productCategory.create.success", null, LocaleContextHolder.getLocale()),
                service.create(dto));
    }

    @Operation(summary = "Cập nhật danh mục sản phẩm", description = "Cập nhật thông tin danh mục theo ID")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    public ApiResponse<ProductCategoryDTO> update(
            @Parameter(description = "ID danh mục cần cập nhật") @PathVariable Integer id,
            @Valid @RequestBody ProductCategoryDTO dto) {
        return ApiResponse.success(
                messageSource.getMessage("productCategory.update.success", null, LocaleContextHolder.getLocale()),
                service.update(id, dto));
    }

    @Operation(summary = "Xóa danh mục sản phẩm",
            description = "Xóa danh mục theo ID. Không xóa được nếu danh mục có con hoặc có sản phẩm.")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @Parameter(description = "ID danh mục cần xóa") @PathVariable Integer id) {
        service.delete(id);
        return ApiResponse.success(
                messageSource.getMessage("productCategory.delete.success", null, LocaleContextHolder.getLocale()),
                null);
    }

    @Operation(summary = "Lấy danh mục theo ID", description = "Lấy thông tin chi tiết danh mục sản phẩm theo ID")
    @GetMapping("/{id}")
    public ApiResponse<ProductCategoryDTO> getById(
            @Parameter(description = "ID danh mục") @PathVariable Integer id) {
        return ApiResponse.success(null, service.getById(id));
    }

    @Operation(summary = "Lấy tất cả danh mục", description = "Lấy toàn bộ danh sách danh mục sản phẩm")
    @GetMapping
    public ApiResponse<List<ProductCategoryDTO>> getAll() {
        return ApiResponse.success(null, service.getAll());
    }

    @Operation(summary = "Lấy danh mục gốc", description = "Lấy danh sách danh mục cấp cao nhất (không có parent)")
    @GetMapping("/roots")
    public ApiResponse<List<ProductCategoryDTO>> getRootCategories() {
        return ApiResponse.success(null, service.getRootCategories());
    }

    @Operation(summary = "Lấy danh mục con", description = "Lấy danh sách danh mục con của một danh mục cha")
    @GetMapping("/{id}/children")
    public ApiResponse<List<ProductCategoryDTO>> getChildren(
            @Parameter(description = "ID danh mục cha") @PathVariable Integer id) {
        return ApiResponse.success(null, service.getChildren(id));
    }
}