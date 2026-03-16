package com.utc.ec.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Thông tin giá trị thuộc tính biến thể (variation option)")
public class VariationOptionDTO {

    @Schema(description = "ID variation option (tự động tạo)", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer id;

    @NotNull(message = "ID variation không được để trống")
    @Schema(description = "ID variation", example = "1")
    private Integer variationId;

    @NotBlank(message = "Giá trị không được để trống")
    @Size(max = 200, message = "Giá trị tối đa 200 ký tự")
    @Schema(description = "Giá trị thuộc tính", example = "Đỏ")
    private String value;
}
