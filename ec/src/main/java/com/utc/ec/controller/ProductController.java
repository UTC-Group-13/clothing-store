package com.utc.ec.controller;

import com.utc.ec.dto.ApiResponse;
import com.utc.ec.dto.PagedResponse;
import com.utc.ec.dto.ProductDTO;
import com.utc.ec.service.ProductService;
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

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Product", description = "API quản lý sản phẩm")
public class ProductController {

    private final ProductService service;
    private final MessageSource messageSource;

    @Operation(summary = "Tạo sản phẩm", description = "Tạo mới sản phẩm. categoryId phải tồn tại, slug phải duy nhất.")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProductDTO> create(@Valid @RequestBody ProductDTO dto) {
        return ApiResponse.success(
                messageSource.getMessage("product.create.success", null, LocaleContextHolder.getLocale()),
                service.create(dto));
    }

    @Operation(summary = "Cập nhật sản phẩm")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    public ApiResponse<ProductDTO> update(
            @Parameter(description = "ID sản phẩm") @PathVariable Integer id,
            @Valid @RequestBody ProductDTO dto) {
        return ApiResponse.success(
                messageSource.getMessage("product.update.success", null, LocaleContextHolder.getLocale()),
                service.update(id, dto));
    }

    @Operation(summary = "Xóa sản phẩm", description = "Không xóa được nếu sản phẩm đang có biến thể.")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@Parameter(description = "ID sản phẩm") @PathVariable Integer id) {
        service.delete(id);
        return ApiResponse.success(
                messageSource.getMessage("product.delete.success", null, LocaleContextHolder.getLocale()),
                null);
    }

    @Operation(summary = "Lấy sản phẩm theo ID")
    @GetMapping("/{id}")
    public ApiResponse<ProductDTO> getById(@Parameter(description = "ID sản phẩm") @PathVariable Integer id) {
        return ApiResponse.success(null, service.getById(id));
    }

    @Operation(summary = "Lấy sản phẩm theo slug")
    @GetMapping("/slug/{slug}")
    public ApiResponse<ProductDTO> getBySlug(@Parameter(description = "Slug sản phẩm") @PathVariable String slug) {
        return ApiResponse.success(null, service.getBySlug(slug));
    }

    @Operation(summary = "Lấy tất cả sản phẩm")
    @GetMapping
    public ApiResponse<List<ProductDTO>> getAll() {
        return ApiResponse.success(null, service.getAll());
    }

    @Operation(summary = "Lấy sản phẩm theo danh mục")
    @GetMapping("/category/{categoryId}")
    public ApiResponse<List<ProductDTO>> getByCategoryId(
            @Parameter(description = "ID danh mục") @PathVariable Integer categoryId) {
        return ApiResponse.success(null, service.getByCategoryId(categoryId));
    }

    @Operation(summary = "Lấy sản phẩm có phân trang")
    @GetMapping("/paged")
    public ApiResponse<PagedResponse<ProductDTO>> getAllPaged(
            @Parameter(description = "Số trang (từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng mỗi trang") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Trường sắp xếp") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Hướng sắp xếp") @RequestParam(defaultValue = "ASC") String direction) {
        return ApiResponse.success(null,
                buildPagedResponse(service.getAllPaged(buildPageable(page, size, sortBy, direction))));
    }

    @Operation(summary = "Tìm kiếm sản phẩm",
            description = "Tìm theo tên sản phẩm, nhiều danh mục, nhiều màu sắc, khoảng giá và trạng thái. Hỗ trợ phân trang và sắp xếp.")
    @GetMapping("/search")
    public ApiResponse<PagedResponse<ProductDTO>> search(
            @Parameter(description = "Tên sản phẩm") @RequestParam(required = false) String name,
            @Parameter(description = "Danh sách ID danh mục (chọn nhiều)") @RequestParam(required = false) List<Integer> categoryIds,
            @Parameter(description = "Giá tối thiểu") @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "Giá tối đa") @RequestParam(required = false) BigDecimal maxPrice,
            @Parameter(description = "Danh sách ID màu sắc (chọn nhiều)") @RequestParam(required = false) List<Integer> colorIds,
            @Parameter(description = "Trạng thái hoạt động") @RequestParam(required = false) Boolean isActive,
            @Parameter(description = "Số trang (từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng mỗi trang") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Trường sắp xếp") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Hướng sắp xếp") @RequestParam(defaultValue = "ASC") String direction) {
        return ApiResponse.success(null,
                buildPagedResponse(service.searchProducts(name, categoryIds, minPrice, maxPrice, colorIds, isActive,
                        buildPageable(page, size, sortBy, direction))));
    }

    private Pageable buildPageable(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("DESC")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        return PageRequest.of(page, size, sort);
    }

    private PagedResponse<ProductDTO> buildPagedResponse(Page<ProductDTO> pagedResult) {
        return PagedResponse.<ProductDTO>builder()
                .content(pagedResult.getContent())
                .pageNumber(pagedResult.getNumber())
                .pageSize(pagedResult.getSize())
                .totalElements(pagedResult.getTotalElements())
                .totalPages(pagedResult.getTotalPages())
                .first(pagedResult.isFirst())
                .last(pagedResult.isLast())
                .build();
    }
}