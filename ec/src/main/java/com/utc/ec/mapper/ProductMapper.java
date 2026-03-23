package com.utc.ec.mapper;

import com.utc.ec.dto.ProductDTO;
import com.utc.ec.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ProductMapper {
    @Mapping(target = "categoryName", ignore = true)
    @Mapping(target = "thumbnailUrl", ignore = true)
    ProductDTO toDto(Product entity);

    Product toEntity(ProductDTO dto);

    List<ProductDTO> toDtoList(List<Product> entities);

    void updateEntityFromDto(ProductDTO dto, @MappingTarget Product entity);
}
