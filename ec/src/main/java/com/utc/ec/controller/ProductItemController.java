package com.utc.ec.controller;

import com.utc.ec.dto.ApiResponse;
import com.utc.ec.dto.PagedResponse;
import com.utc.ec.dto.ProductItemDTO;
import com.utc.ec.service.ProductItemService;
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
@RequestMapping("/api/product-items")
@RequiredArgsConstructor
@Tag(name = "Product Item", description = "API quản lý biến thể sản phẩm")
public class ProductItemController {
    private final ProductItemService service;
    private final MessageSource messageSource;

    @Operation(summary = "Tạo product item", description = "Tạo mới một product item")
    @PostMapping
    public ApiResponse<ProductItemDTO> create(@RequestBody ProductItemDTO dto) {
        return ApiResponse.<ProductItemDTO>builder()
                .success(true)
                .message(messageSource.getMessage("productItem.create.success", null, LocaleContextHolder.getLocale()))
                .data(service.create(dto))
                .build();
    }

    @Operation(summary = "Cập nhật product item", description = "Cập nhật thông tin product item theo ID")
    @PutMapping("/{id}")
    public ApiResponse<ProductItemDTO> update(
            @Parameter(description = "ID product item cần cập nhật") @PathVariable Integer id,
            @RequestBody ProductItemDTO dto) {
        return ApiResponse.<ProductItemDTO>builder()
                .success(true)
                .message(messageSource.getMessage("productItem.update.success", null, LocaleContextHolder.getLocale()))
                .data(service.update(id, dto))
                .build();
    }

    @Operation(summary = "Xóa product item", description = "Xóa product item theo ID")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @Parameter(description = "ID product item cần xóa") @PathVariable Integer id) {
        service.delete(id);
        return ApiResponse.<Void>builder()
                .success(true)
                .message(messageSource.getMessage("productItem.delete.success", null, LocaleContextHolder.getLocale()))
                .build();
    }

    @Operation(summary = "Lấy product item theo ID", description = "Lấy thông tin chi tiết product item theo ID")
    @GetMapping("/{id}")
    public ApiResponse<ProductItemDTO> getById(
            @Parameter(description = "ID product item cần lấy") @PathVariable Integer id) {
        return ApiResponse.<ProductItemDTO>builder()
                .success(true)
                .data(service.getById(id))
                .build();
    }

    @Operation(summary = "Lấy tất cả product items", description = "Lấy danh sách tất cả product items")
    @GetMapping
    public ApiResponse<List<ProductItemDTO>> getAll() {
        return ApiResponse.<List<ProductItemDTO>>builder()
                .success(true)
                .data(service.getAll())
                .build();
    }

    @Operation(summary = "Lấy product items theo product ID", description = "Lấy danh sách product items của một sản phẩm")
    @GetMapping("/product/{productId}")
    public ApiResponse<List<ProductItemDTO>> getByProductId(
            @Parameter(description = "ID sản phẩm") @PathVariable Integer productId) {
        return ApiResponse.<List<ProductItemDTO>>builder()
                .success(true)
                .data(service.getByProductId(productId))
                .build();
    }

    @Operation(summary = "Tìm kiếm product items", description = "Tìm kiếm product items theo từ khóa, product ID và khoảng giá với phân trang")
    @GetMapping("/search")
    public ApiResponse<PagedResponse<ProductItemDTO>> searchProductItems(
            @Parameter(description = "Từ khóa tìm kiếm (SKU)") @RequestParam(required = false) String keyword,
            @Parameter(description = "ID sản phẩm") @RequestParam(required = false) Integer productId,
            @Parameter(description = "Giá tối thiểu") @RequestParam(required = false) Integer minPrice,
            @Parameter(description = "Giá tối đa") @RequestParam(required = false) Integer maxPrice,
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng item mỗi trang") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Trường sắp xếp") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Hướng sắp xếp (ASC/DESC)") @RequestParam(defaultValue = "ASC") String direction) {

        Sort sort = direction.equalsIgnoreCase("DESC") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ProductItemDTO> pagedResult = service.searchProductItems(keyword, productId, minPrice, maxPrice, pageable);

        PagedResponse<ProductItemDTO> response = PagedResponse.<ProductItemDTO>builder()
                .content(pagedResult.getContent())
                .pageNumber(pagedResult.getNumber())
                .pageSize(pagedResult.getSize())
                .totalElements(pagedResult.getTotalElements())
                .totalPages(pagedResult.getTotalPages())
                .last(pagedResult.isLast())
                .first(pagedResult.isFirst())
                .build();

        return ApiResponse.<PagedResponse<ProductItemDTO>>builder()
                .success(true)
                .data(response)
                .build();
    }
}
