package com.utc.ec.entity;

import java.io.Serializable;
import lombok.Data;

@Data
public class ProductConfigurationId implements Serializable {
    private Integer productItemId;
    private Integer variationOptionId;
}

