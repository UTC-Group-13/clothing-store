package com.utc.ec.controller;

import com.utc.ec.dto.ApiResponse;
import com.utc.ec.dto.PagedResponse;
import com.utc.ec.dto.ProductDTO;
import com.utc.ec.service.ProductService;
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

    @Operation(summary = "Lấy tất cả sản phẩm", description = "Lấy danh sách tất cả sản phẩm (không phân trang)")
    @GetMapping
    public ApiResponse<List<ProductDTO>> getAll() {
        return ApiResponse.<List<ProductDTO>>builder()
                .success(true)
                .data(service.getAll())
                .build();
    }

    @Operation(summary = "Lấy sản phẩm có phân trang", description = "Lấy danh sách sản phẩm với phân trang và sắp xếp")
    @GetMapping("/paged")
    public ApiResponse<PagedResponse<ProductDTO>> getAllPaged(
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng item mỗi trang") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Trường sắp xếp") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Hướng sắp xếp (ASC/DESC)") @RequestParam(defaultValue = "ASC") String direction) {

        Sort sort = direction.equalsIgnoreCase("DESC") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ProductDTO> pagedResult = service.getAllPaged(pageable);

        PagedResponse<ProductDTO> response = PagedResponse.<ProductDTO>builder()
                .content(pagedResult.getContent())
                .pageNumber(pagedResult.getNumber())
                .pageSize(pagedResult.getSize())
                .totalElements(pagedResult.getTotalElements())
                .totalPages(pagedResult.getTotalPages())
                .last(pagedResult.isLast())
                .first(pagedResult.isFirst())
                .build();

        return ApiResponse.<PagedResponse<ProductDTO>>builder()
                .success(true)
                .data(response)
                .build();
    }

    @Operation(summary = "Tìm kiếm sản phẩm", description = "Tìm kiếm sản phẩm theo từ khóa và category với phân trang")
    @GetMapping("/search")
    public ApiResponse<PagedResponse<ProductDTO>> searchProducts(
            @Parameter(description = "Từ khóa tìm kiếm (tên, mô tả)") @RequestParam(required = false) String keyword,
            @Parameter(description = "ID danh mục") @RequestParam(required = false) Integer categoryId,
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng item mỗi trang") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Trường sắp xếp") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Hướng sắp xếp (ASC/DESC)") @RequestParam(defaultValue = "ASC") String direction) {

        Sort sort = direction.equalsIgnoreCase("DESC") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ProductDTO> pagedResult = service.searchProducts(keyword, categoryId, pageable);

        PagedResponse<ProductDTO> response = PagedResponse.<ProductDTO>builder()
                .content(pagedResult.getContent())
                .pageNumber(pagedResult.getNumber())
                .pageSize(pagedResult.getSize())
                .totalElements(pagedResult.getTotalElements())
                .totalPages(pagedResult.getTotalPages())
                .last(pagedResult.isLast())
                .first(pagedResult.isFirst())
                .build();

        return ApiResponse.<PagedResponse<ProductDTO>>builder()
                .success(true)
                .data(response)
                .build();
    }
}