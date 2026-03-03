package com.utc.ec.mapper;

import com.utc.ec.dto.ProductConfigurationDTO;
import com.utc.ec.entity.ProductConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ProductConfigurationMapper {
    ProductConfigurationDTO toDto(ProductConfiguration entity);

    ProductConfiguration toEntity(ProductConfigurationDTO dto);

    List<ProductConfigurationDTO> toDtoList(List<ProductConfiguration> entities);
}
