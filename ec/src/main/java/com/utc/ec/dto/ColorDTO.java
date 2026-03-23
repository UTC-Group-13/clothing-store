package com.utc.ec.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Thông tin màu sắc")
public class ColorDTO {

    @Schema(description = "ID màu (tự động tạo)", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer id;

    @NotBlank(message = "Tên màu không được để trống")
    @Size(max = 50, message = "Tên màu tối đa 50 ký tự")
    @Schema(description = "Tên màu sắc", example = "Đỏ")
    private String name;

    @NotBlank(message = "Mã hex không được để trống")
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Mã hex phải có dạng #RRGGBB")
    @Schema(description = "Mã hex màu sắc", example = "#FF0000")
    private String hexCode;

    @NotBlank(message = "Slug không được để trống")
    @Size(max = 50, message = "Slug tối đa 50 ký tự")
    @Schema(description = "Slug màu sắc (URL-friendly)", example = "do")
    private String slug;
}

