package com.utc.ec.controller;

import com.utc.ec.dto.ApiResponse;
import com.utc.ec.dto.ProductDTO;
import com.utc.ec.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService service;
    private final MessageSource messageSource;

    @PostMapping
    public ApiResponse<ProductDTO> create(@RequestBody ProductDTO dto) {
        return ApiResponse.<ProductDTO>builder()
                .success(true)
                .message(messageSource.getMessage("product.create.success", null, LocaleContextHolder.getLocale()))
                .data(service.create(dto))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<ProductDTO> update(@PathVariable Integer id, @RequestBody ProductDTO dto) {
        return ApiResponse.<ProductDTO>builder()
                .success(true)
                .message(messageSource.getMessage("product.update.success", null, LocaleContextHolder.getLocale()))
                .data(service.update(id, dto))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ApiResponse.<Void>builder()
                .success(true)
                .message(messageSource.getMessage("product.delete.success", null, LocaleContextHolder.getLocale()))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductDTO> getById(@PathVariable Integer id) {
        return ApiResponse.<ProductDTO>builder()
                .success(true)
                .data(service.getById(id))
                .build();
    }

    @GetMapping
    public ApiResponse<List<ProductDTO>> getAll() {
        return ApiResponse.<List<ProductDTO>>builder()
                .success(true)
                .data(service.getAll())
                .build();
    }
}
