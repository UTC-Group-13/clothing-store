package com.utc.ec.service;

import com.utc.ec.dto.ProductDTO;
import java.util.List;

public interface ProductService {
    ProductDTO create(ProductDTO dto);
    ProductDTO update(Integer id, ProductDTO dto);
    void delete(Integer id);
    ProductDTO getById(Integer id);
    List<ProductDTO> getAll();
}
