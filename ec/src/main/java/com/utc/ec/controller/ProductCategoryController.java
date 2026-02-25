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
        String message = messageSource.getMessage("productCategory.create.success", null, LocaleContextHolder.getLocale());
        return ApiResponse.success(message, service.create(dto));
    }

    @PutMapping("/{id}")
    public ApiResponse<ProductCategoryDTO> update(@PathVariable Integer id, @RequestBody ProductCategoryDTO dto) {
        String message = messageSource.getMessage("productCategory.update.success", null, LocaleContextHolder.getLocale());
        return ApiResponse.success(message, service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        String message = messageSource.getMessage("productCategory.delete.success", null, LocaleContextHolder.getLocale());
        return ApiResponse.success(message, null);
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductCategoryDTO> getById(@PathVariable Integer id) {
        return ApiResponse.success(null, service.getById(id));
    }

    @GetMapping
    public ApiResponse<List<ProductCategoryDTO>> getAll() {
        return ApiResponse.success(null, service.getAll());
    }
}
