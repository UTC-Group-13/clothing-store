package com.utc.ec.mapper;

import com.utc.ec.dto.ProductItemDTO;
import com.utc.ec.entity.ProductItem;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ProductItemMapper {
    ProductItemDTO toDto(ProductItem entity);

    ProductItem toEntity(ProductItemDTO dto);

    List<ProductItemDTO> toDtoList(List<ProductItem> entities);

    void updateEntityFromDto(ProductItemDTO dto, @MappingTarget ProductItem entity);
}
