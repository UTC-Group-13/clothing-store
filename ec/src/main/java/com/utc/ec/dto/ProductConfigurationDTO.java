package com.utc.ec.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Cấu hình sản phẩm: ánh xạ product item với variation option")
public class ProductConfigurationDTO {

    @NotNull(message = "ID product item không được để trống")
    @Schema(description = "ID product item", example = "101")
    private Integer productItemId;

    @NotNull(message = "ID variation option không được để trống")
    @Schema(description = "ID variation option", example = "1")
    private Integer variationOptionId;
}
