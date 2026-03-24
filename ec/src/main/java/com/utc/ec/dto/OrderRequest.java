package com.utc.ec.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Yêu cầu đặt hàng")
public class OrderRequest {

    @NotNull(message = "paymentTypeId khong duoc de trong")
    @Schema(description = "ID loại thanh toán (COD, Chuyển khoản...)", example = "1")
    private Integer paymentTypeId;

    @NotNull(message = "shippingAddressId khong duoc de trong")
    @Schema(description = "ID địa chỉ giao hàng", example = "1")
    private Integer shippingAddressId;

    @NotNull(message = "shippingMethodId khong duoc de trong")
    @Schema(description = "ID phương thức vận chuyển", example = "1")
    private Integer shippingMethodId;

    @Schema(description = "Ghi chú đơn hàng", example = "Giao giờ hành chính")
    private String note;
}

