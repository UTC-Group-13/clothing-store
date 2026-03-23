package com.utc.ec.mapper;

import com.utc.ec.dto.SizeDTO;
import com.utc.ec.entity.Size;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface SizeMapper {
    SizeDTO toDto(Size entity);

    Size toEntity(SizeDTO dto);

    List<SizeDTO> toDtoList(List<Size> entities);

    void updateEntityFromDto(SizeDTO dto, @MappingTarget Size entity);
}

