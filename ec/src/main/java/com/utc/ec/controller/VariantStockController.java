package com.utc.ec.controller;

import com.utc.ec.dto.ApiResponse;
import com.utc.ec.dto.PagedResponse;
import com.utc.ec.dto.VariantStockDTO;
import com.utc.ec.service.VariantStockService;
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
@RequestMapping("/api/variant-stocks")
@RequiredArgsConstructor
@Tag(name = "Variant Stock", description = "API quản lý tồn kho theo biến thể + size")
public class VariantStockController {

    private final VariantStockService service;
    private final MessageSource messageSource;

    @Operation(summary = "Tạo tồn kho", description = "Thêm tồn kho cho 1 ô (variant × size). SKU phải duy nhất.")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<VariantStockDTO> create(@Valid @RequestBody VariantStockDTO dto) {
        return ApiResponse.success(
                messageSource.getMessage("variantStock.create.success", null, LocaleContextHolder.getLocale()),
                service.create(dto));
    }

    @Operation(summary = "Cập nhật tồn kho")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    public ApiResponse<VariantStockDTO> update(
            @Parameter(description = "ID tồn kho") @PathVariable Integer id,
            @Valid @RequestBody VariantStockDTO dto) {
        return ApiResponse.success(
                messageSource.getMessage("variantStock.update.success", null, LocaleContextHolder.getLocale()),
                service.update(id, dto));
    }

    @Operation(summary = "Xóa tồn kho")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@Parameter(description = "ID tồn kho") @PathVariable Integer id) {
        service.delete(id);
        return ApiResponse.success(
                messageSource.getMessage("variantStock.delete.success", null, LocaleContextHolder.getLocale()),
                null);
    }

    @Operation(summary = "Lấy tồn kho theo ID")
    @GetMapping("/{id}")
    public ApiResponse<VariantStockDTO> getById(@Parameter(description = "ID tồn kho") @PathVariable Integer id) {
        return ApiResponse.success(null, service.getById(id));
    }

    @Operation(summary = "Lấy tất cả tồn kho")
    @GetMapping
    public ApiResponse<List<VariantStockDTO>> getAll() {
        return ApiResponse.success(null, service.getAll());
    }

    @Operation(summary = "Lấy tồn kho theo biến thể")
    @GetMapping("/variant/{variantId}")
    public ApiResponse<List<VariantStockDTO>> getByVariantId(
            @Parameter(description = "ID biến thể") @PathVariable Integer variantId) {
        return ApiResponse.success(null, service.getByVariantId(variantId));
    }

    @Operation(summary = "Tìm kiếm tồn kho", description = "Tìm kiếm theo SKU và/hoặc biến thể với phân trang")
    @GetMapping("/search")
    public ApiResponse<PagedResponse<VariantStockDTO>> search(
            @Parameter(description = "Từ khóa (SKU)") @RequestParam(required = false) String keyword,
            @Parameter(description = "ID biến thể") @RequestParam(required = false) Integer variantId,
            @Parameter(description = "Số trang (từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng mỗi trang") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Trường sắp xếp") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Hướng sắp xếp") @RequestParam(defaultValue = "ASC") String direction) {

        Sort sort = direction.equalsIgnoreCase("DESC") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<VariantStockDTO> pagedResult = service.searchVariantStocks(keyword, variantId, pageable);

        return ApiResponse.success(null, PagedResponse.<VariantStockDTO>builder()
                .content(pagedResult.getContent())
                .pageNumber(pagedResult.getNumber())
                .pageSize(pagedResult.getSize())
                .totalElements(pagedResult.getTotalElements())
                .totalPages(pagedResult.getTotalPages())
                .first(pagedResult.isFirst())
                .last(pagedResult.isLast())
                .build());
    }
}

