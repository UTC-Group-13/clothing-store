package com.utc.ec.service;

import com.utc.ec.dto.SizeDTO;
import java.util.List;

public interface SizeService {
    SizeDTO create(SizeDTO dto);
    SizeDTO update(Integer id, SizeDTO dto);
    void delete(Integer id);
    SizeDTO getById(Integer id);
    List<SizeDTO> getAll();
    List<SizeDTO> getByType(String type);
}

