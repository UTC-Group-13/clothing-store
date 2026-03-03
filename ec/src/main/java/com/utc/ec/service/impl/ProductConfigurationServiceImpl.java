package com.utc.ec.service.impl;

import com.utc.ec.dto.ProductConfigurationDTO;
import com.utc.ec.entity.ProductConfiguration;
import com.utc.ec.entity.ProductConfigurationId;
import com.utc.ec.mapper.ProductConfigurationMapper;
import com.utc.ec.repository.ProductConfigurationRepository;
import com.utc.ec.service.ProductConfigurationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductConfigurationServiceImpl implements ProductConfigurationService {
    private final ProductConfigurationRepository repository;
    private final ProductConfigurationMapper mapper;

    @Override
    public ProductConfigurationDTO create(ProductConfigurationDTO dto) {
        ProductConfiguration entity = mapper.toEntity(dto);
        ProductConfiguration saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    public void delete(ProductConfigurationId id) {
        repository.deleteById(id);
    }

    @Override
    public ProductConfigurationDTO getById(ProductConfigurationId id) {
        ProductConfiguration entity = repository.findById(id).orElseThrow();
        return mapper.toDto(entity);
    }

    @Override
    public List<ProductConfigurationDTO> getAll() {
        return mapper.toDtoList(repository.findAll());
    }

    @Override
    public Page<ProductConfigurationDTO> getAllPaged(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDto);
    }

    @Override
    public List<ProductConfigurationDTO> getByProductItemId(Integer productItemId) {
        return mapper.toDtoList(repository.findByProductItemId(productItemId));
    }

    @Override
    public List<ProductConfigurationDTO> getByVariationOptionId(Integer variationOptionId) {
        return mapper.toDtoList(repository.findByVariationOptionId(variationOptionId));
    }
}
