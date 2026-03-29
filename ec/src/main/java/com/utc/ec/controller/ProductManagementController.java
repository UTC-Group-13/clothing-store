package com.utc.ec.controller;

import com.utc.ec.dto.ApiResponse;
import com.utc.ec.dto.ProductDetailResponse;
import com.utc.ec.dto.ProductFullRequest;
import com.utc.ec.service.ProductManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Product Management", description = "API tao/cap nhat san pham day du (kem bien the + ton kho)")
@SecurityRequirement(name = "bearerAuth")
public class ProductManagementController {

    private final ProductManagementService service;
    private final MessageSource messageSource;

    // ────────────────────────────────────────────────────────────────
    // POST /api/products/full  →  Tao san pham day du
    // ────────────────────────────────────────────────────────────────
    @Operation(
            summary = "Tao san pham day du [ADMIN]",
            description = """
                    Tao moi san pham kem bien the mau sac va ton kho theo size trong 1 request duy nhat.
                    
                    **Quy trinh:**
                    1. Khai bao thong tin Product (name, slug, basePrice, categoryId...)
                    2. Them danh sach `variants` - moi variant tuong ung 1 mau sac
                    3. Moi variant co danh sach `stocks` - moi stock tuong ung 1 size (kem so luong + SKU)
                    
                    **Luu y:**
                    - Chi duoc co 1 variant co `isDefault = true`
                    - `slug` phai duy nhat trong he thong
                    - `sku` trong moi stock phai duy nhat toan cuc
                    - Yeu cau quyen **ADMIN**
                    """
    )
    @PostMapping("/full")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ProductDetailResponse> createFull(
            @Valid @RequestBody ProductFullRequest request) {
        return ApiResponse.success(
                messageSource.getMessage("product.create.success", null, LocaleContextHolder.getLocale()),
                service.createFull(request));
    }

    // ────────────────────────────────────────────────────────────────
    // PUT /api/products/full/{id}  →  Cap nhat san pham day du
    // ────────────────────────────────────────────────────────────────
    @Operation(
            summary = "Cap nhat san pham day du [ADMIN]",
            description = """
                    Cap nhat san pham + bien the + ton kho.
                    
                    **Quy tac xu ly variants / stocks:**
                    - Neu `id` co gia tri → cap nhat ban ghi da ton tai
                    - Neu `id = null`     → tao moi
                    - Ban ghi khong co trong request → **giu nguyen** (khong bi xoa)
                    
                    **De xoa bien the / stock**, dung endpoint rieng:
                    - `DELETE /api/product-variants/{variantId}`
                    - `DELETE /api/variant-stocks/{stockId}`
                    
                    Yeu cau quyen **ADMIN**
                    """
    )
    @PutMapping("/full/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ProductDetailResponse> updateFull(
            @Parameter(description = "ID san pham can cap nhat") @PathVariable Integer id,
            @Valid @RequestBody ProductFullRequest request) {
        return ApiResponse.success(
                messageSource.getMessage("product.update.success", null, LocaleContextHolder.getLocale()),
                service.updateFull(id, request));
    }

    // ────────────────────────────────────────────────────────────────
    // GET /api/products/full/{id}  →  Lay chi tiet day du san pham
    // ────────────────────────────────────────────────────────────────
    @Operation(
            summary = "Lay chi tiet day du san pham",
            description = """
                    Tra ve toan bo thong tin san pham kem danh sach bien the mau sac va ton kho theo size.
                    Endpoint nay public (khong can dang nhap).
                    """
    )
    @GetMapping("/full/{id}")
    public ApiResponse<ProductDetailResponse> getFullById(
            @Parameter(description = "ID san pham") @PathVariable Integer id) {
        return ApiResponse.success(null, service.getFullById(id));
    }
}

