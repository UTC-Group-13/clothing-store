package com.utc.ec.mapper;

import com.utc.ec.dto.ColorDTO;
import com.utc.ec.entity.Color;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ColorMapper {
    ColorDTO toDto(Color entity);

    Color toEntity(ColorDTO dto);

    List<ColorDTO> toDtoList(List<Color> entities);

    void updateEntityFromDto(ColorDTO dto, @MappingTarget Color entity);
}

