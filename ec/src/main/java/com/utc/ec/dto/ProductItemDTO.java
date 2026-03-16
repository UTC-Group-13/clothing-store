package com.utc.ec.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Thông tin biến thể sản phẩm (product item)")
public class ProductItemDTO {

    @Schema(description = "ID product item (tự động tạo)", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer id;

    @NotNull(message = "ID sản phẩm không được để trống")
    @Schema(description = "ID sản phẩm cha", example = "10")
    private Integer productId;

    @NotBlank(message = "SKU không được để trống")
    @Size(max = 20, message = "SKU tối đa 20 ký tự")
    @Schema(description = "Mã SKU (duy nhất)", example = "ATB-RED-M")
    private String sku;

    @Min(value = 0, message = "Số lượng tồn kho không được âm")
    @Schema(description = "Số lượng tồn kho", example = "50")
    private Integer qtyInStock;

    @Schema(description = "URL ảnh biến thể", example = "/uploads/images/ao-thun-do-m.jpg")
    private String productImage;

    @NotNull(message = "Giá không được để trống")
    @Min(value = 0, message = "Giá không được âm")
    @Schema(description = "Giá bán (VNĐ)", example = "150000")
    private Integer price;
}
