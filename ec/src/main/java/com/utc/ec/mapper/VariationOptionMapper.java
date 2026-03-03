package com.utc.ec.mapper;

import com.utc.ec.dto.VariationOptionDTO;
import com.utc.ec.entity.VariationOption;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface VariationOptionMapper {
    VariationOptionDTO toDto(VariationOption entity);

    VariationOption toEntity(VariationOptionDTO dto);

    List<VariationOptionDTO> toDtoList(List<VariationOption> entities);

    void updateEntityFromDto(VariationOptionDTO dto, @MappingTarget VariationOption entity);
}
