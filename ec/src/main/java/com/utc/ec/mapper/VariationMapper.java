package com.utc.ec.mapper;

import com.utc.ec.dto.VariationDTO;
import com.utc.ec.entity.Variation;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface VariationMapper {
    VariationDTO toDto(Variation entity);

    Variation toEntity(VariationDTO dto);

    List<VariationDTO> toDtoList(List<Variation> entities);

    void updateEntityFromDto(VariationDTO dto, @MappingTarget Variation entity);
}
