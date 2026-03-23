package com.utc.ec.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Schema(description = "Thông tin tồn kho theo biến thể + size")
public class VariantStockDTO {

    @Schema(description = "ID (tự động tạo)", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer id;

    @NotNull(message = "ID variant không được để trống")
    @Schema(description = "ID product variant", example = "1")
    private Integer variantId;

    @NotNull(message = "ID size không được để trống")
    @Schema(description = "ID size", example = "2")
    private Integer sizeId;

    @Min(value = 0, message = "Số lượng tồn kho không được âm")
    @Schema(description = "Số lượng tồn kho", example = "50")
    private Integer stockQty = 0;

    @Schema(description = "Giá riêng cho size/màu này (null = dùng base_price)", example = "299000")
    private BigDecimal priceOverride;

    @NotBlank(message = "SKU không được để trống")
    @Size(max = 100, message = "SKU tối đa 100 ký tự")
    @Schema(description = "Mã hàng duy nhất", example = "ATB-DO-M")
    private String sku;

    // --- Read-only enriched fields ---
    @Schema(description = "Label size", accessMode = Schema.AccessMode.READ_ONLY)
    private String sizeLabel;

    @Schema(description = "Loại size", accessMode = Schema.AccessMode.READ_ONLY)
    private String sizeType;
}

