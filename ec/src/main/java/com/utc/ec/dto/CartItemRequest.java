package com.utc.ec.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartItemRequest {

    @NotNull(message = "variantStockId khong duoc de trong")
    private Integer variantStockId;

    @NotNull(message = "qty khong duoc de trong")
    @Min(value = 1, message = "qty phai >= 1")
    private Integer qty;
}

