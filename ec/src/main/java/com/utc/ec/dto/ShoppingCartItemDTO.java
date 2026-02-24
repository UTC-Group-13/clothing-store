package com.utc.ec.dto;

import lombok.Data;

@Data
public class ShoppingCartItemDTO {
    private Integer id;
    private Integer cartId;
    private Integer productItemId;
    private Integer qty;
}

