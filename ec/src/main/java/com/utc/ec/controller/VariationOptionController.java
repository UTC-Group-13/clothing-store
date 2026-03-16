package com.utc.ec.controller;

import com.utc.ec.dto.ApiResponse;
import com.utc.ec.dto.PagedResponse;
import com.utc.ec.dto.VariationOptionDTO;
import com.utc.ec.service.VariationOptionService;
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
@RequestMapping("/api/variation-options")
@RequiredArgsConstructor
@Tag(name = "Variation Option", description = "API quản lý giá trị thuộc tính biến thể")
public class VariationOptionController {

    private final VariationOptionService service;
    private final MessageSource messageSource;

    @Operation(summary = "Tạo variation option",
            description = "Tạo mới một giá trị thuộc tính. variationId phải tồn tại và value không được trùng trong cùng variation.")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<VariationOptionDTO> create(@Valid @RequestBody VariationOptionDTO dto) {
        return ApiResponse.success(
                messageSource.getMessage("variationOption.create.success", null, LocaleContextHolder.getLocale()),
                service.create(dto));
    }

    @Operation(summary = "Cập nhật variation option", description = "Cập nhật thông tin giá trị thuộc tính theo ID")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    public ApiResponse<VariationOptionDTO> update(
            @Parameter(description = "ID variation option cần cập nhật") @PathVariable Integer id,
            @Valid @RequestBody VariationOptionDTO dto) {
        return ApiResponse.success(
                messageSource.getMessage("variationOption.update.success", null, LocaleContextHolder.getLocale()),
                service.update(id, dto));
    }

    @Operation(summary = "Xóa variation option",
            description = "Xóa variation option theo ID. Không xóa được nếu đang được dùng trong product configuration.")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @Parameter(description = "ID variation option cần xóa") @PathVariable Integer id) {
        service.delete(id);
        return ApiResponse.success(
                messageSource.getMessage("variationOption.delete.success", null, LocaleContextHolder.getLocale()),
                null);
    }

    @Operation(summary = "Lấy variation option theo ID", description = "Lấy thông tin chi tiết variation option theo ID")
    @GetMapping("/{id}")
    public ApiResponse<VariationOptionDTO> getById(
            @Parameter(description = "ID variation option") @PathVariable Integer id) {
        return ApiResponse.success(null, service.getById(id));
    }

    @Operation(summary = "Lấy tất cả variation options", description = "Lấy danh sách tất cả variation options")
    @GetMapping
    public ApiResponse<List<VariationOptionDTO>> getAll() {
        return ApiResponse.success(null, service.getAll());
    }

    @Operation(summary = "Lấy variation options theo variation", description = "Lấy danh sách các giá trị của một thuộc tính")
    @GetMapping("/variation/{variationId}")
    public ApiResponse<List<VariationOptionDTO>> getByVariationId(
            @Parameter(description = "ID variation") @PathVariable Integer variationId) {
        return ApiResponse.success(null, service.getByVariationId(variationId));
    }

    @Operation(summary = "Tìm kiếm variation options", description = "Tìm kiếm variation options theo từ khóa và variation ID với phân trang")
    @GetMapping("/search")
    public ApiResponse<PagedResponse<VariationOptionDTO>> search(
            @Parameter(description = "Từ khóa tìm kiếm (giá trị)") @RequestParam(required = false) String keyword,
            @Parameter(description = "ID variation") @RequestParam(required = false) Integer variationId,
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng mỗi trang") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Trường sắp xếp") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Hướng sắp xếp (ASC/DESC)") @RequestParam(defaultValue = "ASC") String direction) {

        Sort sort = direction.equalsIgnoreCase("DESC") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<VariationOptionDTO> pagedResult = service.searchVariationOptions(keyword, variationId, pageable);

        return ApiResponse.success(null, PagedResponse.<VariationOptionDTO>builder()
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
