package com.utc.ec.dto;

import lombok.Data;

@Data
public class OrderLineDTO {
    private Integer id;
    private Integer productItemId;
    private Integer orderId;
    private Integer qty;
    private Integer price;
}
