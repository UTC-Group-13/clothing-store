package com.utc.ec.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "colors")
public class Color {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "hex_code", nullable = false, length = 7)
    private String hexCode;

    @Column(name = "slug", nullable = false, unique = true, length = 50)
    private String slug;
}

