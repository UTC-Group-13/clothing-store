package com.utc.ec.dto;

import lombok.Data;

@Data
public class ShoppingCartItemDTO {
    private Integer id;
    private Integer cartId;
    private Integer variantStockId;
    private Integer qty;
}
