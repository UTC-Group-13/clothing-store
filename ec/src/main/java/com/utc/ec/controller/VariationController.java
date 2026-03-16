package com.utc.ec.controller;

import com.utc.ec.dto.ApiResponse;
import com.utc.ec.dto.PagedResponse;
import com.utc.ec.dto.VariationDTO;
import com.utc.ec.service.VariationService;
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
@RequestMapping("/api/variations")
@RequiredArgsConstructor
@Tag(name = "Variation", description = "API quản lý thuộc tính biến thể")
public class VariationController {

    private final VariationService service;
    private final MessageSource messageSource;

    @Operation(summary = "Tạo variation", description = "Tạo mới một thuộc tính biến thể. categoryId phải tồn tại và tên không được trùng trong cùng danh mục.")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<VariationDTO> create(@Valid @RequestBody VariationDTO dto) {
        return ApiResponse.success(
                messageSource.getMessage("variation.create.success", null, LocaleContextHolder.getLocale()),
                service.create(dto));
    }

    @Operation(summary = "Cập nhật variation", description = "Cập nhật thông tin thuộc tính biến thể theo ID")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    public ApiResponse<VariationDTO> update(
            @Parameter(description = "ID variation cần cập nhật") @PathVariable Integer id,
            @Valid @RequestBody VariationDTO dto) {
        return ApiResponse.success(
                messageSource.getMessage("variation.update.success", null, LocaleContextHolder.getLocale()),
                service.update(id, dto));
    }

    @Operation(summary = "Xóa variation", description = "Xóa variation theo ID. Không xóa được nếu đang có variation option.")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @Parameter(description = "ID variation cần xóa") @PathVariable Integer id) {
        service.delete(id);
        return ApiResponse.success(
                messageSource.getMessage("variation.delete.success", null, LocaleContextHolder.getLocale()),
                null);
    }

    @Operation(summary = "Lấy variation theo ID", description = "Lấy thông tin chi tiết variation theo ID")
    @GetMapping("/{id}")
    public ApiResponse<VariationDTO> getById(
            @Parameter(description = "ID variation") @PathVariable Integer id) {
        return ApiResponse.success(null, service.getById(id));
    }

    @Operation(summary = "Lấy tất cả variations", description = "Lấy danh sách tất cả variations")
    @GetMapping
    public ApiResponse<List<VariationDTO>> getAll() {
        return ApiResponse.success(null, service.getAll());
    }

    @Operation(summary = "Lấy variations theo danh mục", description = "Lấy danh sách variations của một danh mục")
    @GetMapping("/category/{categoryId}")
    public ApiResponse<List<VariationDTO>> getByCategoryId(
            @Parameter(description = "ID danh mục") @PathVariable Integer categoryId) {
        return ApiResponse.success(null, service.getByCategoryId(categoryId));
    }

    @Operation(summary = "Tìm kiếm variations", description = "Tìm kiếm variations theo từ khóa và danh mục với phân trang")
    @GetMapping("/search")
    public ApiResponse<PagedResponse<VariationDTO>> search(
            @Parameter(description = "Từ khóa tìm kiếm (tên)") @RequestParam(required = false) String keyword,
            @Parameter(description = "ID danh mục") @RequestParam(required = false) Integer categoryId,
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng mỗi trang") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Trường sắp xếp") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Hướng sắp xếp (ASC/DESC)") @RequestParam(defaultValue = "ASC") String direction) {

        Sort sort = direction.equalsIgnoreCase("DESC") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<VariationDTO> pagedResult = service.searchVariations(keyword, categoryId, pageable);

        return ApiResponse.success(null, PagedResponse.<VariationDTO>builder()
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
