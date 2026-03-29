package com.utc.ec.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "Chi tiet day du san pham kem bien the va ton kho")
public class ProductDetailResponse {

    // ======================= Product =======================
    @Schema(description = "ID san pham")
    private Integer id;

    @Schema(description = "Ten san pham")
    private String name;

    @Schema(description = "Slug")
    private String slug;

    @Schema(description = "Mo ta san pham")
    private String description;

    @Schema(description = "ID danh muc")
    private Integer categoryId;

    @Schema(description = "Ten danh muc")
    private String categoryName;

    @Schema(description = "Gia goc (VND)")
    private BigDecimal basePrice;

    @Schema(description = "Thuong hieu")
    private String brand;

    @Schema(description = "Chat lieu")
    private String material;

    @Schema(description = "Trang thai hoat dong")
    private Boolean isActive;

    @Schema(description = "Ngay tao (yyyy-MM-dd HH:mm:ss)")
    private String createdAt;

    @Schema(description = "Ngay cap nhat (yyyy-MM-dd HH:mm:ss)")
    private String updatedAt;

    @Schema(description = "Danh sach bien the theo mau sac")
    private List<VariantDetail> variants;

    // ─────────────────────────────────────────────────────────────

    @Data
    @Schema(description = "Chi tiet bien the theo mau sac")
    public static class VariantDetail {

        @Schema(description = "ID bien the")
        private Integer id;

        @Schema(description = "ID mau sac")
        private Integer colorId;

        @Schema(description = "Ten mau sac")
        private String colorName;

        @Schema(description = "Ma hex mau sac", example = "#FF0000")
        private String colorHexCode;

        @Schema(description = "Slug mau sac")
        private String colorSlug;

        @Schema(description = "URL anh thumbnail cho mau nay")
        private String colorImageUrl;

        @Schema(description = "Danh sach URL anh chi tiet (JSON array string)")
        private String images;

        @Schema(description = "Bien the mac dinh?")
        private Boolean isDefault;

        @Schema(description = "Danh sach ton kho theo size")
        private List<StockDetail> stocks;
    }

    // ─────────────────────────────────────────────────────────────

    @Data
    @Schema(description = "Chi tiet ton kho theo size")
    public static class StockDetail {

        @Schema(description = "ID ton kho")
        private Integer id;

        @Schema(description = "ID size")
        private Integer sizeId;

        @Schema(description = "Nhan size", example = "M")
        private String sizeLabel;

        @Schema(description = "Loai size", example = "clothing")
        private String sizeType;

        @Schema(description = "So luong ton kho")
        private Integer stockQty;

        @Schema(description = "Gia rieng (null = dung base_price)")
        private BigDecimal priceOverride;

        @Schema(description = "Gia thuc te ap dung (priceOverride neu co, nguoc lai la basePrice)")
        private BigDecimal effectivePrice;

        @Schema(description = "Ma SKU")
        private String sku;
    }
}

