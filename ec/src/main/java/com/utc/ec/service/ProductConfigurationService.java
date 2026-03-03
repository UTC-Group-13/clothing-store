package com.utc.ec.service;

import com.utc.ec.dto.ProductConfigurationDTO;
import com.utc.ec.entity.ProductConfigurationId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductConfigurationService {
    ProductConfigurationDTO create(ProductConfigurationDTO dto);
    void delete(ProductConfigurationId id);
    ProductConfigurationDTO getById(ProductConfigurationId id);
    List<ProductConfigurationDTO> getAll();
    Page<ProductConfigurationDTO> getAllPaged(Pageable pageable);
    List<ProductConfigurationDTO> getByProductItemId(Integer productItemId);
    List<ProductConfigurationDTO> getByVariationOptionId(Integer variationOptionId);
}
