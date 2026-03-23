package com.utc.ec.service;

import com.utc.ec.dto.ColorDTO;
import java.util.List;

public interface ColorService {
    ColorDTO create(ColorDTO dto);
    ColorDTO update(Integer id, ColorDTO dto);
    void delete(Integer id);
    ColorDTO getById(Integer id);
    List<ColorDTO> getAll();
}

