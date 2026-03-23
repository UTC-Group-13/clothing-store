package com.utc.ec.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Thông tin danh mục sản phẩm")
public class CategoryDTO {

    @Schema(description = "ID danh mục (tự động tạo)", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer id;

    @NotBlank(message = "Tên danh mục không được để trống")
    @Size(max = 100, message = "Tên danh mục tối đa 100 ký tự")
    @Schema(description = "Tên danh mục", example = "Áo")
    private String name;

    @NotBlank(message = "Slug không được để trống")
    @Size(max = 100, message = "Slug tối đa 100 ký tự")
    @Schema(description = "Slug danh mục (URL-friendly)", example = "ao")
    private String slug;

    @Schema(description = "ID danh mục cha (null nếu là danh mục gốc)", example = "1")
    private Integer parentId;

    @Schema(description = "Mô tả danh mục", example = "Danh mục áo các loại")
    private String description;

    @Schema(description = "Ngày tạo", accessMode = Schema.AccessMode.READ_ONLY)
    private String createdAt;
}

