package com.utc.ec.controller;

import com.utc.ec.dto.ApiResponse;
import com.utc.ec.dto.PagedResponse;
import com.utc.ec.dto.ProductConfigurationDTO;
import com.utc.ec.entity.ProductConfigurationId;
import com.utc.ec.service.ProductConfigurationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product-configurations")
@RequiredArgsConstructor
@Tag(name = "Product Configuration", description = "API quản lý cấu hình sản phẩm (gắn product item với variation option)")
public class ProductConfigurationController {

    private final ProductConfigurationService service;
    private final MessageSource messageSource;

    @Operation(summary = "Tạo product configuration",
            description = "Gắn một product item với một variation option. Cả hai ID đều phải tồn tại và chưa được gắn với nhau.")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProductConfigurationDTO> create(@Valid @RequestBody ProductConfigurationDTO dto) {
        return ApiResponse.success(
                messageSource.getMessage("productConfiguration.create.success", null, LocaleContextHolder.getLocale()),
                service.create(dto));
    }

    @Operation(summary = "Xóa product configuration",
            description = "Xóa liên kết giữa product item và variation option theo composite key")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{productItemId}/{variationOptionId}")
    public ApiResponse<Void> delete(
            @Parameter(description = "ID product item") @PathVariable Integer productItemId,
            @Parameter(description = "ID variation option") @PathVariable Integer variationOptionId) {
        ProductConfigurationId id = buildId(productItemId, variationOptionId);
        service.delete(id);
        return ApiResponse.success(
                messageSource.getMessage("productConfiguration.delete.success", null, LocaleContextHolder.getLocale()),
                null);
    }

    @Operation(summary = "Lấy product configuration theo composite key",
            description = "Lấy thông tin cấu hình theo productItemId và variationOptionId")
    @GetMapping("/{productItemId}/{variationOptionId}")
    public ApiResponse<ProductConfigurationDTO> getById(
            @Parameter(description = "ID product item") @PathVariable Integer productItemId,
            @Parameter(description = "ID variation option") @PathVariable Integer variationOptionId) {
        return ApiResponse.success(null, service.getById(buildId(productItemId, variationOptionId)));
    }

    @Operation(summary = "Lấy tất cả product configurations", description = "Lấy danh sách tất cả cấu hình sản phẩm (không phân trang)")
    @GetMapping
    public ApiResponse<List<ProductConfigurationDTO>> getAll() {
        return ApiResponse.success(null, service.getAll());
    }

    @Operation(summary = "Lấy product configurations có phân trang", description = "Lấy danh sách cấu hình sản phẩm với phân trang")
    @GetMapping("/paged")
    public ApiResponse<PagedResponse<ProductConfigurationDTO>> getAllPaged(
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng mỗi trang") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Trường sắp xếp") @RequestParam(defaultValue = "productItemId") String sortBy,
            @Parameter(description = "Hướng sắp xếp (ASC/DESC)") @RequestParam(defaultValue = "ASC") String direction) {

        Sort sort = direction.equalsIgnoreCase("DESC") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ProductConfigurationDTO> pagedResult = service.getAllPaged(pageable);

        return ApiResponse.success(null, PagedResponse.<ProductConfigurationDTO>builder()
                .content(pagedResult.getContent())
                .pageNumber(pagedResult.getNumber())
                .pageSize(pagedResult.getSize())
                .totalElements(pagedResult.getTotalElements())
                .totalPages(pagedResult.getTotalPages())
                .first(pagedResult.isFirst())
                .last(pagedResult.isLast())
                .build());
    }

    @Operation(summary = "Lấy configurations theo product item",
            description = "Lấy danh sách tất cả thuộc tính của một product item (biến thể)")
    @GetMapping("/product-item/{productItemId}")
    public ApiResponse<List<ProductConfigurationDTO>> getByProductItemId(
            @Parameter(description = "ID product item") @PathVariable Integer productItemId) {
        return ApiResponse.success(null, service.getByProductItemId(productItemId));
    }

    @Operation(summary = "Lấy configurations theo variation option",
            description = "Lấy danh sách product item đang dùng một variation option cụ thể")
    @GetMapping("/variation-option/{variationOptionId}")
    public ApiResponse<List<ProductConfigurationDTO>> getByVariationOptionId(
            @Parameter(description = "ID variation option") @PathVariable Integer variationOptionId) {
        return ApiResponse.success(null, service.getByVariationOptionId(variationOptionId));
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private ProductConfigurationId buildId(Integer productItemId, Integer variationOptionId) {
        ProductConfigurationId id = new ProductConfigurationId();
        id.setProductItemId(productItemId);
        id.setVariationOptionId(variationOptionId);
        return id;
    }
}
