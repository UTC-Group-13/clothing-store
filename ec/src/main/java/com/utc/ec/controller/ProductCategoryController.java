package com.utc.ec.controller;

import com.utc.ec.dto.ApiResponse;
import com.utc.ec.dto.ProductCategoryDTO;
import com.utc.ec.service.ProductCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product-categories")
@RequiredArgsConstructor
public class ProductCategoryController {
    private final ProductCategoryService service;
    private final MessageSource messageSource;

    @PostMapping
    public ApiResponse<ProductCategoryDTO> create(@RequestBody ProductCategoryDTO dto) {
        return ApiResponse.<ProductCategoryDTO>builder()
                .success(true)
                .message(messageSource.getMessage("productCategory.create.success", null, LocaleContextHolder.getLocale()))
                .data(service.create(dto))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<ProductCategoryDTO> update(@PathVariable Integer id, @RequestBody ProductCategoryDTO dto) {
        return ApiResponse.<ProductCategoryDTO>builder()
                .success(true)
                .message(messageSource.getMessage("productCategory.update.success", null, LocaleContextHolder.getLocale()))
                .data(service.update(id, dto))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ApiResponse.<Void>builder()
                .success(true)
                .message(messageSource.getMessage("productCategory.delete.success", null, LocaleContextHolder.getLocale()))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductCategoryDTO> getById(@PathVariable Integer id) {
        return ApiResponse.<ProductCategoryDTO>builder()
                .success(true)
                .data(service.getById(id))
                .build();
    }

    @GetMapping
    public ApiResponse<List<ProductCategoryDTO>> getAll() {
        return ApiResponse.<List<ProductCategoryDTO>>builder()
                .success(true)
                .data(service.getAll())
                .build();
    }
}
