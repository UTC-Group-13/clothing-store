package com.utc.ec.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Thông tin thuộc tính biến thể (variation)")
public class VariationDTO {

    @Schema(description = "ID variation (tự động tạo)", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer id;

    @NotNull(message = "ID danh mục không được để trống")
    @Schema(description = "ID danh mục sản phẩm", example = "2")
    private Integer categoryId;

    @NotBlank(message = "Tên thuộc tính không được để trống")
    @Size(max = 500, message = "Tên thuộc tính tối đa 500 ký tự")
    @Schema(description = "Tên thuộc tính", example = "Màu Sắc")
    private String name;
}
