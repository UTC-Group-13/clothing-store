package com.utc.ec.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Thông tin danh mục sản phẩm")
public class ProductCategoryDTO {

    @Schema(description = "ID danh mục (tự động tạo)", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer id;

    @Schema(description = "ID danh mục cha (null nếu là danh mục gốc)", example = "1")
    private Integer parentCategoryId;

    @NotBlank(message = "Tên danh mục không được để trống")
    @Size(max = 200, message = "Tên danh mục tối đa 200 ký tự")
    @Schema(description = "Tên danh mục", example = "Áo Thun")
    private String categoryName;
}
