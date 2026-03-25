package com.utc.ec.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Yeu cau tao danh gia san pham")
public class CreateReviewRequest {

    @NotNull(message = "orderedProductId khong duoc de trong")
    @Schema(description = "ID cua order_line (dong san pham da mua)", example = "1")
    private Integer orderedProductId;

    @NotNull(message = "ratingValue khong duoc de trong")
    @Min(value = 1, message = "Diem danh gia phai tu 1 den 5")
    @Max(value = 5, message = "Diem danh gia phai tu 1 den 5")
    @Schema(description = "Diem danh gia (1-5 sao)", example = "5")
    private Integer ratingValue;

    @Size(max = 2000, message = "Noi dung danh gia khong qua 2000 ky tu")
    @Schema(description = "Noi dung danh gia", example = "San pham rat dep, chat luong tot!")
    private String comment;
}

