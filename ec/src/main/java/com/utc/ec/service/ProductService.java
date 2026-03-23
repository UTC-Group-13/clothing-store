package com.utc.ec.service;

import com.utc.ec.dto.ProductDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {
    ProductDTO create(ProductDTO dto);
    ProductDTO update(Integer id, ProductDTO dto);
    void delete(Integer id);
    ProductDTO getById(Integer id);
    ProductDTO getBySlug(String slug);
    List<ProductDTO> getAll();
    List<ProductDTO> getByCategoryId(Integer categoryId);
    Page<ProductDTO> getAllPaged(Pageable pageable);
    Page<ProductDTO> searchProducts(String name, List<Integer> categoryIds, BigDecimal minPrice,
                                    BigDecimal maxPrice, List<Integer> colorIds, Boolean isActive,
                                    Pageable pageable);
}
