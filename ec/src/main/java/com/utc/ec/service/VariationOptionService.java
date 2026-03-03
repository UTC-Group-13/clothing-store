package com.utc.ec.service;

import com.utc.ec.dto.VariationOptionDTO;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VariationOptionService {
    VariationOptionDTO create(VariationOptionDTO dto);
    VariationOptionDTO update(Integer id, VariationOptionDTO dto);
    void delete(Integer id);
    VariationOptionDTO getById(Integer id);
    List<VariationOptionDTO> getAll();
    List<VariationOptionDTO> getByVariationId(Integer variationId);
    Page<VariationOptionDTO> searchVariationOptions(String keyword, Integer variationId, Pageable pageable);
}
