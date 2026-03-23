package com.utc.ec.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "Thông tin biến thể sản phẩm (theo màu)")
public class ProductVariantDTO {

    @Schema(description = "ID variant (tự động tạo)", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer id;

    @NotNull(message = "ID sản phẩm không được để trống")
    @Schema(description = "ID sản phẩm", example = "1")
    private Integer productId;

    @NotNull(message = "ID màu sắc không được để trống")
    @Schema(description = "ID màu sắc", example = "3")
    private Integer colorId;

    @Schema(description = "URL ảnh thumbnail cho màu này", example = "/uploads/images/ao-thun-do.jpg")
    private String colorImageUrl;

    @Schema(description = "Danh sách URL ảnh chi tiết cho màu này")
    private String images;

    @Schema(description = "Có phải màu mặc định không", example = "false")
    private Boolean isDefault = false;

    // --- Read-only enriched fields ---
    @Schema(description = "Tên màu sắc", accessMode = Schema.AccessMode.READ_ONLY)
    private String colorName;

    @Schema(description = "Mã hex màu sắc", accessMode = Schema.AccessMode.READ_ONLY)
    private String colorHexCode;
}

