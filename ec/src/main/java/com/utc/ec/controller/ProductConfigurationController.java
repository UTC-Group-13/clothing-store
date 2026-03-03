package com.utc.ec.controller;

import com.utc.ec.dto.ApiResponse;
import com.utc.ec.dto.PagedResponse;
import com.utc.ec.dto.ProductConfigurationDTO;
import com.utc.ec.entity.ProductConfigurationId;
import com.utc.ec.service.ProductConfigurationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product-configurations")
@RequiredArgsConstructor
@Tag(name = "Product Configuration", description = "API quản lý cấu hình sản phẩm")
public class ProductConfigurationController {
    private final ProductConfigurationService service;
    private final MessageSource messageSource;

    @Operation(summary = "Tạo product configuration", description = "Tạo mới một product configuration")
    @PostMapping
    public ApiResponse<ProductConfigurationDTO> create(@RequestBody ProductConfigurationDTO dto) {
        return ApiResponse.<ProductConfigurationDTO>builder()
                .success(true)
                .message(messageSource.getMessage("productConfiguration.create.success", null, LocaleContextHolder.getLocale()))
                .data(service.create(dto))
                .build();
    }

    @Operation(summary = "Xóa product configuration", description = "Xóa product configuration theo composite key")
    @DeleteMapping("/{productItemId}/{variationOptionId}")
    public ApiResponse<Void> delete(
            @Parameter(description = "ID product item") @PathVariable Integer productItemId,
            @Parameter(description = "ID variation option") @PathVariable Integer variationOptionId) {
        ProductConfigurationId id = new ProductConfigurationId();
        id.setProductItemId(productItemId);
        id.setVariationOptionId(variationOptionId);
        service.delete(id);
        return ApiResponse.<Void>builder()
                .success(true)
                .message(messageSource.getMessage("productConfiguration.delete.success", null, LocaleContextHolder.getLocale()))
                .build();
    }

    @Operation(summary = "Lấy product configuration theo ID", description = "Lấy thông tin chi tiết product configuration theo composite key")
    @GetMapping("/{productItemId}/{variationOptionId}")
    public ApiResponse<ProductConfigurationDTO> getById(
            @Parameter(description = "ID product item") @PathVariable Integer productItemId,
            @Parameter(description = "ID variation option") @PathVariable Integer variationOptionId) {
        ProductConfigurationId id = new ProductConfigurationId();
        id.setProductItemId(productItemId);
        id.setVariationOptionId(variationOptionId);
        return ApiResponse.<ProductConfigurationDTO>builder()
                .success(true)
                .data(service.getById(id))
                .build();
    }

    @Operation(summary = "Lấy tất cả product configurations", description = "Lấy danh sách tất cả product configurations (không phân trang)")
    @GetMapping
    public ApiResponse<List<ProductConfigurationDTO>> getAll() {
        return ApiResponse.<List<ProductConfigurationDTO>>builder()
                .success(true)
                .data(service.getAll())
                .build();
    }

    @Operation(summary = "Lấy product configurations có phân trang", description = "Lấy danh sách product configurations với phân trang và sắp xếp")
    @GetMapping("/paged")
    public ApiResponse<PagedResponse<ProductConfigurationDTO>> getAllPaged(
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng item mỗi trang") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Trường sắp xếp") @RequestParam(defaultValue = "productItemId") String sortBy,
            @Parameter(description = "Hướng sắp xếp (ASC/DESC)") @RequestParam(defaultValue = "ASC") String direction) {

        Sort sort = direction.equalsIgnoreCase("DESC") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ProductConfigurationDTO> pagedResult = service.getAllPaged(pageable);

        PagedResponse<ProductConfigurationDTO> response = PagedResponse.<ProductConfigurationDTO>builder()
                .content(pagedResult.getContent())
                .pageNumber(pagedResult.getNumber())
                .pageSize(pagedResult.getSize())
                .totalElements(pagedResult.getTotalElements())
                .totalPages(pagedResult.getTotalPages())
                .last(pagedResult.isLast())
                .first(pagedResult.isFirst())
                .build();

        return ApiResponse.<PagedResponse<ProductConfigurationDTO>>builder()
                .success(true)
                .data(response)
                .build();
    }

    @Operation(summary = "Lấy product configurations theo product item ID", description = "Lấy danh sách configurations của một product item")
    @GetMapping("/product-item/{productItemId}")
    public ApiResponse<List<ProductConfigurationDTO>> getByProductItemId(
            @Parameter(description = "ID product item") @PathVariable Integer productItemId) {
        return ApiResponse.<List<ProductConfigurationDTO>>builder()
                .success(true)
                .data(service.getByProductItemId(productItemId))
                .build();
    }

    @Operation(summary = "Lấy product configurations theo variation option ID", description = "Lấy danh sách configurations của một variation option")
    @GetMapping("/variation-option/{variationOptionId}")
    public ApiResponse<List<ProductConfigurationDTO>> getByVariationOptionId(
            @Parameter(description = "ID variation option") @PathVariable Integer variationOptionId) {
        return ApiResponse.<List<ProductConfigurationDTO>>builder()
                .success(true)
                .data(service.getByVariationOptionId(variationOptionId))
                .build();
    }
}
