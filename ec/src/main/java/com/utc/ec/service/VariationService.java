package com.utc.ec.service;

import com.utc.ec.dto.VariationDTO;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VariationService {
    VariationDTO create(VariationDTO dto);
    VariationDTO update(Integer id, VariationDTO dto);
    void delete(Integer id);
    VariationDTO getById(Integer id);
    List<VariationDTO> getAll();
    List<VariationDTO> getByCategoryId(Integer categoryId);
    Page<VariationDTO> searchVariations(String keyword, Integer categoryId, Pageable pageable);
}
