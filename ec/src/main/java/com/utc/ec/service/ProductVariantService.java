package com.utc.ec.service;

import com.utc.ec.dto.ProductVariantDTO;
import java.util.List;

public interface ProductVariantService {
    ProductVariantDTO create(ProductVariantDTO dto);
    ProductVariantDTO update(Integer id, ProductVariantDTO dto);
    void delete(Integer id);
    ProductVariantDTO getById(Integer id);
    List<ProductVariantDTO> getAll();
    List<ProductVariantDTO> getByProductId(Integer productId);
}

