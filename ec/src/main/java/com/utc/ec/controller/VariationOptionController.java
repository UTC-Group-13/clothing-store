package com.utc.ec.controller;

import com.utc.ec.dto.ApiResponse;
import com.utc.ec.dto.PagedResponse;
import com.utc.ec.dto.VariationOptionDTO;
import com.utc.ec.service.VariationOptionService;
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
@RequestMapping("/api/variation-options")
@RequiredArgsConstructor
@Tag(name = "Variation Option", description = "API quản lý giá trị thuộc tính biến thể")
public class VariationOptionController {
    private final VariationOptionService service;
    private final MessageSource messageSource;

    @Operation(summary = "Tạo variation option", description = "Tạo mới một variation option")
    @PostMapping
    public ApiResponse<VariationOptionDTO> create(@RequestBody VariationOptionDTO dto) {
        return ApiResponse.<VariationOptionDTO>builder()
                .success(true)
                .message(messageSource.getMessage("variationOption.create.success", null, LocaleContextHolder.getLocale()))
                .data(service.create(dto))
                .build();
    }

    @Operation(summary = "Cập nhật variation option", description = "Cập nhật thông tin variation option theo ID")
    @PutMapping("/{id}")
    public ApiResponse<VariationOptionDTO> update(
            @Parameter(description = "ID variation option cần cập nhật") @PathVariable Integer id,
            @RequestBody VariationOptionDTO dto) {
        return ApiResponse.<VariationOptionDTO>builder()
                .success(true)
                .message(messageSource.getMessage("variationOption.update.success", null, LocaleContextHolder.getLocale()))
                .data(service.update(id, dto))
                .build();
    }

    @Operation(summary = "Xóa variation option", description = "Xóa variation option theo ID")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @Parameter(description = "ID variation option cần xóa") @PathVariable Integer id) {
        service.delete(id);
        return ApiResponse.<Void>builder()
                .success(true)
                .message(messageSource.getMessage("variationOption.delete.success", null, LocaleContextHolder.getLocale()))
                .build();
    }

    @Operation(summary = "Lấy variation option theo ID", description = "Lấy thông tin chi tiết variation option theo ID")
    @GetMapping("/{id}")
    public ApiResponse<VariationOptionDTO> getById(
            @Parameter(description = "ID variation option cần lấy") @PathVariable Integer id) {
        return ApiResponse.<VariationOptionDTO>builder()
                .success(true)
                .data(service.getById(id))
                .build();
    }

    @Operation(summary = "Lấy tất cả variation options", description = "Lấy danh sách tất cả variation options")
    @GetMapping
    public ApiResponse<List<VariationOptionDTO>> getAll() {
        return ApiResponse.<List<VariationOptionDTO>>builder()
                .success(true)
                .data(service.getAll())
                .build();
    }

    @Operation(summary = "Lấy variation options theo variation ID", description = "Lấy danh sách variation options của một variation")
    @GetMapping("/variation/{variationId}")
    public ApiResponse<List<VariationOptionDTO>> getByVariationId(
            @Parameter(description = "ID variation") @PathVariable Integer variationId) {
        return ApiResponse.<List<VariationOptionDTO>>builder()
                .success(true)
                .data(service.getByVariationId(variationId))
                .build();
    }

    @Operation(summary = "Tìm kiếm variation options", description = "Tìm kiếm variation options theo từ khóa và variation ID với phân trang")
    @GetMapping("/search")
    public ApiResponse<PagedResponse<VariationOptionDTO>> searchVariationOptions(
            @Parameter(description = "Từ khóa tìm kiếm (giá trị)") @RequestParam(required = false) String keyword,
            @Parameter(description = "ID variation") @RequestParam(required = false) Integer variationId,
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng item mỗi trang") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Trường sắp xếp") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Hướng sắp xếp (ASC/DESC)") @RequestParam(defaultValue = "ASC") String direction) {

        Sort sort = direction.equalsIgnoreCase("DESC") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<VariationOptionDTO> pagedResult = service.searchVariationOptions(keyword, variationId, pageable);

        PagedResponse<VariationOptionDTO> response = PagedResponse.<VariationOptionDTO>builder()
                .content(pagedResult.getContent())
                .pageNumber(pagedResult.getNumber())
                .pageSize(pagedResult.getSize())
                .totalElements(pagedResult.getTotalElements())
                .totalPages(pagedResult.getTotalPages())
                .last(pagedResult.isLast())
                .first(pagedResult.isFirst())
                .build();

        return ApiResponse.<PagedResponse<VariationOptionDTO>>builder()
                .success(true)
                .data(response)
                .build();
    }
}
