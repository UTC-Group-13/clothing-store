package com.utc.ec.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateOrderStatusRequest {

    @NotNull(message = "statusId khong duoc de trong")
    private Integer statusId;
}

