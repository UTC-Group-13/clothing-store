package com.utc.ec.mapper;

import com.utc.ec.dto.VariantStockDTO;
import com.utc.ec.entity.VariantStock;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface VariantStockMapper {
    @Mapping(target = "sizeLabel", ignore = true)
    @Mapping(target = "sizeType", ignore = true)
    VariantStockDTO toDto(VariantStock entity);

    VariantStock toEntity(VariantStockDTO dto);

    List<VariantStockDTO> toDtoList(List<VariantStock> entities);

    void updateEntityFromDto(VariantStockDTO dto, @MappingTarget VariantStock entity);
}
