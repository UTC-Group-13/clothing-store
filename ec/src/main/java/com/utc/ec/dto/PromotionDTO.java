package com.utc.ec.dto;

import lombok.Data;

@Data
public class PromotionDTO {
    private Integer id;
    private String name;
    private String description;
    private Integer discountRate;
    private java.time.LocalDateTime startDate;
    private java.time.LocalDateTime endDate;
}

