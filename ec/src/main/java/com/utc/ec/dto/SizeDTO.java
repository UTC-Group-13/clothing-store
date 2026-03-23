package com.utc.ec.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Thông tin size sản phẩm")
public class SizeDTO {

    @Schema(description = "ID size (tự động tạo)", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer id;

    @NotBlank(message = "Label không được để trống")
    @Size(max = 20, message = "Label tối đa 20 ký tự")
    @Schema(description = "Nhãn size", example = "M")
    private String label;

    @NotBlank(message = "Loại size không được để trống")
    @Size(max = 20, message = "Loại size tối đa 20 ký tự")
    @Schema(description = "Loại size: clothing | numeric | shoes", example = "clothing")
    private String type;

    @NotNull(message = "Thứ tự sắp xếp không được để trống")
    @Min(value = 0, message = "Thứ tự sắp xếp không được âm")
    @Schema(description = "Thứ tự sắp xếp", example = "2")
    private Integer sortOrder = 0;
}

