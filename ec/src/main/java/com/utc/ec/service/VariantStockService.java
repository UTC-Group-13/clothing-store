package com.utc.ec.service;

import com.utc.ec.dto.VariantStockDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface VariantStockService {
    VariantStockDTO create(VariantStockDTO dto);
    VariantStockDTO update(Integer id, VariantStockDTO dto);
    void delete(Integer id);
    VariantStockDTO getById(Integer id);
    List<VariantStockDTO> getAll();
    List<VariantStockDTO> getByVariantId(Integer variantId);
    Page<VariantStockDTO> searchVariantStocks(String keyword, Integer variantId, Pageable pageable);
}

