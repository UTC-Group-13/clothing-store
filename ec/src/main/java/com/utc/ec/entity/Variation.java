package com.utc.ec.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "variation")
public class Variation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "category_id")
    private Integer categoryId;

    private String name;
}

