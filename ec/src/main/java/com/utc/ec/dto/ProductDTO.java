package com.utc.ec.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Schema(description = "Thông tin sản phẩm")
public class ProductDTO {

    @Schema(description = "ID sản phẩm (tự động tạo)", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer id;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 200, message = "Tên sản phẩm tối đa 200 ký tự")
    @Schema(description = "Tên sản phẩm", example = "Áo Thun Basic Cotton")
    private String name;

    @NotBlank(message = "Slug không được để trống")
    @Size(max = 200, message = "Slug tối đa 200 ký tự")
    @Schema(description = "Slug sản phẩm (URL-friendly)", example = "ao-thun-basic-cotton")
    private String slug;

    @Schema(description = "Mô tả sản phẩm", example = "Áo thun cotton 100%, thoáng mát")
    private String description;

    @NotNull(message = "ID danh mục không được để trống")
    @Schema(description = "ID danh mục sản phẩm", example = "2")
    private Integer categoryId;

    @NotNull(message = "Giá gốc không được để trống")
    @Schema(description = "Giá gốc sản phẩm", example = "199000")
    private BigDecimal basePrice;

    @Size(max = 100, message = "Thương hiệu tối đa 100 ký tự")
    @Schema(description = "Thương hiệu", example = "Uniqlo")
    private String brand;

    @Size(max = 100, message = "Chất liệu tối đa 100 ký tự")
    @Schema(description = "Chất liệu", example = "Cotton 100%")
    private String material;

    @Schema(description = "Trạng thái hoạt động", example = "true")
    private Boolean isActive = true;

    @Schema(description = "Ngày tạo", accessMode = Schema.AccessMode.READ_ONLY)
    private String createdAt;

    @Schema(description = "Ngày cập nhật", accessMode = Schema.AccessMode.READ_ONLY)
    private String updatedAt;

    // --- Read-only enriched fields ---
    @Schema(description = "Tên danh mục", accessMode = Schema.AccessMode.READ_ONLY)
    private String categoryName;

    @Schema(description = "Ảnh đại diện sản phẩm (lấy từ biến thể mặc định)", accessMode = Schema.AccessMode.READ_ONLY)
    private String thumbnailUrl;
}
