package com.utc.ec.controller;

import com.utc.ec.dto.ApiResponse;
import com.utc.ec.dto.CategoryDTO;
import com.utc.ec.service.CategoryService;
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
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Category", description = "API quản lý danh mục sản phẩm")
public class CategoryController {

    private final CategoryService service;
    private final MessageSource messageSource;

    @Operation(summary = "Tạo danh mục", description = "Tạo mới danh mục. parentId = null nếu là danh mục gốc.")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CategoryDTO> create(@Valid @RequestBody CategoryDTO dto) {
        return ApiResponse.success(
                messageSource.getMessage("category.create.success", null, LocaleContextHolder.getLocale()),
                service.create(dto));
    }

    @Operation(summary = "Cập nhật danh mục")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    public ApiResponse<CategoryDTO> update(
            @Parameter(description = "ID danh mục") @PathVariable Integer id,
            @Valid @RequestBody CategoryDTO dto) {
        return ApiResponse.success(
                messageSource.getMessage("category.update.success", null, LocaleContextHolder.getLocale()),
                service.update(id, dto));
    }

    @Operation(summary = "Xóa danh mục", description = "Không xóa được nếu có danh mục con hoặc sản phẩm.")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@Parameter(description = "ID danh mục") @PathVariable Integer id) {
        service.delete(id);
        return ApiResponse.success(
                messageSource.getMessage("category.delete.success", null, LocaleContextHolder.getLocale()),
                null);
    }

    @Operation(summary = "Lấy danh mục theo ID")
    @GetMapping("/{id}")
    public ApiResponse<CategoryDTO> getById(@Parameter(description = "ID danh mục") @PathVariable Integer id) {
        return ApiResponse.success(null, service.getById(id));
    }

    @Operation(summary = "Lấy danh mục theo slug")
    @GetMapping("/slug/{slug}")
    public ApiResponse<CategoryDTO> getBySlug(@Parameter(description = "Slug danh mục") @PathVariable String slug) {
        return ApiResponse.success(null, service.getBySlug(slug));
    }

    @Operation(summary = "Lấy tất cả danh mục")
    @GetMapping
    public ApiResponse<List<CategoryDTO>> getAll() {
        return ApiResponse.success(null, service.getAll());
    }

    @Operation(summary = "Lấy danh mục gốc", description = "Lấy danh mục cấp cao nhất (không có parent)")
    @GetMapping("/roots")
    public ApiResponse<List<CategoryDTO>> getRootCategories() {
        return ApiResponse.success(null, service.getRootCategories());
    }

    @Operation(summary = "Lấy danh mục con")
    @GetMapping("/{id}/children")
    public ApiResponse<List<CategoryDTO>> getChildren(
            @Parameter(description = "ID danh mục cha") @PathVariable Integer id) {
        return ApiResponse.success(null, service.getChildren(id));
    }
}

