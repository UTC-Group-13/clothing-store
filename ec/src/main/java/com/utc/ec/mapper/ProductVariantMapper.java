package com.utc.ec.mapper;

import com.utc.ec.dto.ProductVariantDTO;
import com.utc.ec.entity.ProductVariant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ProductVariantMapper {
    @Mapping(target = "colorName", ignore = true)
    @Mapping(target = "colorHexCode", ignore = true)
    ProductVariantDTO toDto(ProductVariant entity);

    ProductVariant toEntity(ProductVariantDTO dto);

    List<ProductVariantDTO> toDtoList(List<ProductVariant> entities);

    void updateEntityFromDto(ProductVariantDTO dto, @MappingTarget ProductVariant entity);
}
