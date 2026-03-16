package com.utc.ec.service.impl;

import com.utc.ec.dto.ProductConfigurationDTO;
import com.utc.ec.entity.ProductConfiguration;
import com.utc.ec.entity.ProductConfigurationId;
import com.utc.ec.exception.BusinessException;
import com.utc.ec.exception.ResourceNotFoundException;
import com.utc.ec.mapper.ProductConfigurationMapper;
import com.utc.ec.repository.ProductConfigurationRepository;
import com.utc.ec.repository.ProductItemRepository;
import com.utc.ec.repository.VariationOptionRepository;
import com.utc.ec.service.ProductConfigurationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductConfigurationServiceImpl implements ProductConfigurationService {

    private final ProductConfigurationRepository repository;
    private final ProductItemRepository productItemRepository;
    private final VariationOptionRepository variationOptionRepository;
    private final ProductConfigurationMapper mapper;

    @Override
    @Transactional
    public ProductConfigurationDTO create(ProductConfigurationDTO dto) {
        // Validate product_item tồn tại
        if (!productItemRepository.existsById(dto.getProductItemId())) {
            throw new ResourceNotFoundException("productItem.notFound", dto.getProductItemId());
        }
        // Validate variation_option tồn tại
        if (!variationOptionRepository.existsById(dto.getVariationOptionId())) {
            throw new ResourceNotFoundException("variationOption.notFound", dto.getVariationOptionId());
        }
        // Validate không trùng (đã tồn tại)
        ProductConfigurationId id = new ProductConfigurationId();
        id.setProductItemId(dto.getProductItemId());
        id.setVariationOptionId(dto.getVariationOptionId());
        if (repository.existsById(id)) {
            throw new BusinessException("productConfiguration.alreadyExists");
        }

        ProductConfiguration entity = mapper.toEntity(dto);
        return mapper.toDto(repository.save(entity));
    }

    @Override
    @Transactional
    public void delete(ProductConfigurationId id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("productConfiguration.notFound");
        }
        repository.deleteById(id);
    }

    @Override
    public ProductConfigurationDTO getById(ProductConfigurationId id) {
        return mapper.toDto(repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("productConfiguration.notFound")));
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
        if (!productItemRepository.existsById(productItemId)) {
            throw new ResourceNotFoundException("productItem.notFound", productItemId);
        }
        return mapper.toDtoList(repository.findByProductItemId(productItemId));
    }

    @Override
    public List<ProductConfigurationDTO> getByVariationOptionId(Integer variationOptionId) {
        if (!variationOptionRepository.existsById(variationOptionId)) {
            throw new ResourceNotFoundException("variationOption.notFound", variationOptionId);
        }
        return mapper.toDtoList(repository.findByVariationOptionId(variationOptionId));
    }
}
