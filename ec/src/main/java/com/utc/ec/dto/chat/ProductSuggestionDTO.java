package com.utc.ec.dto.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "Sản phẩm được AI gợi ý")
public class ProductSuggestionDTO {

    @Schema(description = "ID sản phẩm", example = "5")
    private Integer id;

    @Schema(description = "Tên sản phẩm", example = "Áo Thun Basic Cotton")
    private String name;

    @Schema(description = "Giá sản phẩm", example = "250000")
    private BigDecimal price;

    @Schema(description = "Ảnh đại diện", example = "/uploads/images/ao-thun.jpg")
    private String thumbnailUrl;

    @Schema(description = "Slug sản phẩm (dùng để tạo link)", example = "ao-thun-basic-cotton")
    private String slug;

    @Schema(description = "Thương hiệu", example = "Uniqlo")
    private String brand;

    @Schema(description = "Chất liệu", example = "Cotton 100%")
    private String material;

    @Schema(description = "Tên danh mục", example = "Áo Thun")
    private String categoryName;
}

