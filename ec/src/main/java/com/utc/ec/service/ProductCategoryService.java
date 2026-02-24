package com.utc.ec.service;

import com.utc.ec.dto.ProductCategoryDTO;
import java.util.List;

public interface ProductCategoryService {
    ProductCategoryDTO create(ProductCategoryDTO dto);
    ProductCategoryDTO update(Integer id, ProductCategoryDTO dto);
    void delete(Integer id);
    ProductCategoryDTO getById(Integer id);
    List<ProductCategoryDTO> getAll();
}
