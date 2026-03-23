package com.utc.ec.service.impl;

import com.utc.ec.dto.ProductVariantDTO;
import com.utc.ec.entity.ProductVariant;
import com.utc.ec.exception.BusinessException;
import com.utc.ec.exception.ResourceNotFoundException;
import com.utc.ec.mapper.ProductVariantMapper;
import com.utc.ec.repository.ColorRepository;
import com.utc.ec.repository.ProductRepository;
import com.utc.ec.repository.ProductVariantRepository;
import com.utc.ec.repository.VariantStockRepository;
import com.utc.ec.service.ProductVariantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductVariantServiceImpl implements ProductVariantService {

    private final ProductVariantRepository repository;
    private final ProductRepository productRepository;
    private final ColorRepository colorRepository;
    private final VariantStockRepository variantStockRepository;
    private final ProductVariantMapper mapper;

    @Override
    @Transactional
    public ProductVariantDTO create(ProductVariantDTO dto) {
        if (!productRepository.existsById(dto.getProductId())) {
            throw new ResourceNotFoundException("productVariant.productNotFound", dto.getProductId());
        }
        if (!colorRepository.existsById(dto.getColorId())) {
            throw new ResourceNotFoundException("productVariant.colorNotFound", dto.getColorId());
        }
        if (repository.existsByProductIdAndColorId(dto.getProductId(), dto.getColorId())) {
            throw new BusinessException("productVariant.duplicateColor");
        }

        ProductVariant entity = mapper.toEntity(dto);
        entity.setId(null);
        if (entity.getIsDefault() == null) entity.setIsDefault(false);
        return mapper.toDto(repository.save(entity));
    }

    @Override
    @Transactional
    public ProductVariantDTO update(Integer id, ProductVariantDTO dto) {
        ProductVariant entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("productVariant.notFound", id));

        if (dto.getProductId() != null && !dto.getProductId().equals(entity.getProductId())) {
            if (!productRepository.existsById(dto.getProductId())) {
                throw new ResourceNotFoundException("productVariant.productNotFound", dto.getProductId());
            }
        }
        if (dto.getColorId() != null && !dto.getColorId().equals(entity.getColorId())) {
            if (!colorRepository.existsById(dto.getColorId())) {
                throw new ResourceNotFoundException("productVariant.colorNotFound", dto.getColorId());
            }
            Integer productId = dto.getProductId() != null ? dto.getProductId() : entity.getProductId();
            if (repository.existsByProductIdAndColorIdAndIdNot(productId, dto.getColorId(), id)) {
                throw new BusinessException("productVariant.duplicateColor");
            }
        }

        mapper.updateEntityFromDto(dto, entity);
        entity.setId(id);
        return mapper.toDto(repository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("productVariant.notFound", id);
        }
        if (variantStockRepository.existsByVariantId(id)) {
            throw new BusinessException("productVariant.hasStocks");
        }
        repository.deleteById(id);
    }

    @Override
    public ProductVariantDTO getById(Integer id) {
        return mapper.toDto(repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("productVariant.notFound", id)));
    }

    @Override
    public List<ProductVariantDTO> getAll() {
        return mapper.toDtoList(repository.findAll());
    }

    @Override
    public List<ProductVariantDTO> getByProductId(Integer productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("productVariant.productNotFound", productId);
        }
        return mapper.toDtoList(repository.findByProductId(productId));
    }
}

