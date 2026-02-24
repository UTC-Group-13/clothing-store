package com.utc.ec.dto;

import lombok.Data;

@Data
public class UserReviewDTO {
    private Integer id;
    private Integer userId;
    private Integer orderedProductId;
    private Integer ratingValue;
    private String comment;
}

