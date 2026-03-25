package com.utc.ec.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReviewResponseDTO {
    private Integer id;
    private Integer userId;
    private String username;
    private Integer orderedProductId;
    private Integer ratingValue;
    private String comment;
    private LocalDateTime createdAt;

    // Thong tin san pham (join tu order_line → variant_stock → variant → product/color/size)
    private Integer productId;
    private String productName;
    private String productSlug;
    private String colorName;
    private String colorHex;
    private String colorImageUrl;
    private String sizeLabel;
    private String sizeType;
    private String sku;
}

