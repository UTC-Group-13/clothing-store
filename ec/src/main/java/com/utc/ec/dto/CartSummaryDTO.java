package com.utc.ec.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CartSummaryDTO {

    private Integer cartId;
    private Integer userId;
    private List<CartItemDetailDTO> items;
    private int totalItems;
    private BigDecimal totalAmount;
}

