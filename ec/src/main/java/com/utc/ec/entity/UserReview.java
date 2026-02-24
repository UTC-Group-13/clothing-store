package com.utc.ec.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "user_review")
public class UserReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "user_id") // FK -> site_user(id)
    private Integer userId;

    @Column(name = "ordered_product_id") // FK -> order_line(id)
    private Integer orderedProductId;

    @Column(name = "rating_value")
    private Integer ratingValue;

    @Column(name = "comment", length = 2000)
    private String comment;
}

