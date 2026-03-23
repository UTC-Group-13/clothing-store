package com.utc.ec.mapper;

import com.utc.ec.dto.CategoryDTO;
import com.utc.ec.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface CategoryMapper {
    CategoryDTO toDto(Category entity);

    Category toEntity(CategoryDTO dto);

    List<CategoryDTO> toDtoList(List<Category> entities);

    void updateEntityFromDto(CategoryDTO dto, @MappingTarget Category entity);
}

