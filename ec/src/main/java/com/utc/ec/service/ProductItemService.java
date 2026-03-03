package com.utc.ec.service;

import com.utc.ec.dto.ProductItemDTO;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductItemService {
    ProductItemDTO create(ProductItemDTO dto);
    ProductItemDTO update(Integer id, ProductItemDTO dto);
    void delete(Integer id);
    ProductItemDTO getById(Integer id);
    List<ProductItemDTO> getAll();
    List<ProductItemDTO> getByProductId(Integer productId);
    Page<ProductItemDTO> searchProductItems(String keyword, Integer productId, Integer minPrice, Integer maxPrice, Pageable pageable);
}
