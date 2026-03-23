package com.utc.ec.service;

import com.utc.ec.dto.CategoryDTO;
import java.util.List;

public interface CategoryService {
    CategoryDTO create(CategoryDTO dto);
    CategoryDTO update(Integer id, CategoryDTO dto);
    void delete(Integer id);
    CategoryDTO getById(Integer id);
    CategoryDTO getBySlug(String slug);
    List<CategoryDTO> getAll();
    List<CategoryDTO> getRootCategories();
    List<CategoryDTO> getChildren(Integer parentId);
}

