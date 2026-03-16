package com.utc.ec.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Thông tin sản phẩm")
public class ProductDTO {

    @Schema(description = "ID sản phẩm (tự động tạo)", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer id;

    @Schema(description = "ID danh mục sản phẩm", example = "2")
    private Integer categoryId;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 500, message = "Tên sản phẩm tối đa 500 ký tự")
    @Schema(description = "Tên sản phẩm", example = "Áo Thun Basic")
    private String name;

    @Size(max = 4000, message = "Mô tả tối đa 4000 ký tự")
    @Schema(description = "Mô tả sản phẩm", example = "Áo thun cotton 100%, thoáng mát, phù hợp mặc hàng ngày")
    private String description;

    @Schema(description = "URL ảnh đại diện sản phẩm", example = "/uploads/images/ao-thun-basic.jpg")
    private String productImage;
}
