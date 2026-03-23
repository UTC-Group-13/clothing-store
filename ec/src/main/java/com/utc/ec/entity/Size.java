package com.utc.ec.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "sizes")
public class Size {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "label", nullable = false, length = 20)
    private String label;

    @Column(name = "type", nullable = false, length = 20)
    private String type;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;
}

