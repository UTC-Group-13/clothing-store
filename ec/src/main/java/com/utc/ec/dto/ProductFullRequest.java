package com.utc.ec.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "Request tao/cap nhat san pham day du (kem bien the va ton kho)")
public class ProductFullRequest {

    // ======================= Product fields =======================

    @NotBlank(message = "Ten san pham khong duoc de trong")
    @Size(max = 200, message = "Ten san pham toi da 200 ky tu")
    @Schema(description = "Ten san pham", example = "Ao Thun Basic Cotton", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotBlank(message = "Slug khong duoc de trong")
    @Size(max = 200, message = "Slug toi da 200 ky tu")
    @Schema(description = "Slug URL-friendly (khong dau, cach boi '-')", example = "ao-thun-basic-cotton",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String slug;

    @Schema(description = "Mo ta san pham", example = "Ao thun cotton 100%, thoang mat")
    private String description;

    @NotNull(message = "ID danh muc khong duoc de trong")
    @Schema(description = "ID danh muc san pham", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer categoryId;

    @NotNull(message = "Gia goc khong duoc de trong")
    @DecimalMin(value = "0.0", message = "Gia phai >= 0")
    @Schema(description = "Gia goc san pham (VND)", example = "199000", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal basePrice;

    @Size(max = 100, message = "Thuong hieu toi da 100 ky tu")
    @Schema(description = "Thuong hieu", example = "Nike")
    private String brand;

    @Size(max = 100, message = "Chat lieu toi da 100 ky tu")
    @Schema(description = "Chat lieu", example = "Cotton 100%")
    private String material;

    @Schema(description = "Trang thai hoat dong (mac dinh: true)", example = "true")
    private Boolean isActive = true;

    // ======================= Variants =======================

    @Valid
    @Schema(description = "Danh sach bien the theo mau sac")
    private List<VariantRequest> variants;

    // ─────────────────────────────────────────────────────────────

    @Data
    @Schema(description = "Bien the theo mau sac")
    public static class VariantRequest {

        @Schema(description = "ID bien the (null = tao moi, co gia tri = cap nhat)", example = "null")
        private Integer id;

        @NotNull(message = "ID mau sac khong duoc de trong")
        @Schema(description = "ID mau sac", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer colorId;

        @Schema(description = "URL anh thumbnail cho mau nay", example = "/uploads/images/ao-do.jpg")
        private String colorImageUrl;

        @Schema(description = "Danh sach URL anh chi tiet (JSON array string)",
                example = "[\"/uploads/images/img1.jpg\",\"/uploads/images/img2.jpg\"]")
        private String images;

        @Schema(description = "Bien the mac dinh hien thi khi vao trang san pham", example = "true")
        private Boolean isDefault = false;

        @Valid
        @Schema(description = "Danh sach ton kho theo size")
        private List<StockRequest> stocks;
    }

    // ─────────────────────────────────────────────────────────────

    @Data
    @Schema(description = "Ton kho theo size")
    public static class StockRequest {

        @Schema(description = "ID ton kho (null = tao moi, co gia tri = cap nhat)", example = "null")
        private Integer id;

        @NotNull(message = "ID size khong duoc de trong")
        @Schema(description = "ID size", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer sizeId;

        @Min(value = 0, message = "So luong ton kho khong duoc am")
        @Schema(description = "So luong ton kho", example = "50")
        private Integer stockQty = 0;

        @DecimalMin(value = "0.0", message = "Gia override phai >= 0")
        @Schema(description = "Gia rieng cho variant nay (null = dung base_price)", example = "null")
        private BigDecimal priceOverride;

        @NotBlank(message = "SKU khong duoc de trong")
        @Size(max = 100, message = "SKU toi da 100 ky tu")
        @Schema(description = "Ma hang duy nhat", example = "ATB-DO-S", requiredMode = Schema.RequiredMode.REQUIRED)
        private String sku;
    }
}

