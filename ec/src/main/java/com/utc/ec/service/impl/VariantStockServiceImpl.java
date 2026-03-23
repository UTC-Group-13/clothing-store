package com.utc.ec.service.impl;

import com.utc.ec.dto.VariantStockDTO;
import com.utc.ec.entity.VariantStock;
import com.utc.ec.exception.BusinessException;
import com.utc.ec.exception.ResourceNotFoundException;
import com.utc.ec.mapper.VariantStockMapper;
import com.utc.ec.repository.ProductVariantRepository;
import com.utc.ec.repository.SizeRepository;
import com.utc.ec.repository.VariantStockRepository;
import com.utc.ec.service.VariantStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VariantStockServiceImpl implements VariantStockService {

    private final VariantStockRepository repository;
    private final ProductVariantRepository variantRepository;
    private final SizeRepository sizeRepository;
    private final VariantStockMapper mapper;

    @Override
    @Transactional
    public VariantStockDTO create(VariantStockDTO dto) {
        if (!variantRepository.existsById(dto.getVariantId())) {
            throw new ResourceNotFoundException("variantStock.variantNotFound", dto.getVariantId());
        }
        if (!sizeRepository.existsById(dto.getSizeId())) {
            throw new ResourceNotFoundException("variantStock.sizeNotFound", dto.getSizeId());
        }
        if (repository.existsByVariantIdAndSizeId(dto.getVariantId(), dto.getSizeId())) {
            throw new BusinessException("variantStock.duplicateVariantSize");
        }
        if (repository.existsBySku(dto.getSku())) {
            throw new BusinessException("variantStock.skuExists", dto.getSku());
        }

        VariantStock entity = mapper.toEntity(dto);
        entity.setId(null);
        if (entity.getStockQty() == null) entity.setStockQty(0);
        return mapper.toDto(repository.save(entity));
    }

    @Override
    @Transactional
    public VariantStockDTO update(Integer id, VariantStockDTO dto) {
        VariantStock entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("variantStock.notFound", id));

        if (dto.getVariantId() != null && !dto.getVariantId().equals(entity.getVariantId())) {
            if (!variantRepository.existsById(dto.getVariantId())) {
                throw new ResourceNotFoundException("variantStock.variantNotFound", dto.getVariantId());
            }
        }
        if (dto.getSizeId() != null && !dto.getSizeId().equals(entity.getSizeId())) {
            if (!sizeRepository.existsById(dto.getSizeId())) {
                throw new ResourceNotFoundException("variantStock.sizeNotFound", dto.getSizeId());
            }
            Integer variantId = dto.getVariantId() != null ? dto.getVariantId() : entity.getVariantId();
            if (repository.existsByVariantIdAndSizeIdAndIdNot(variantId, dto.getSizeId(), id)) {
                throw new BusinessException("variantStock.duplicateVariantSize");
            }
        }
        if (dto.getSku() != null && !dto.getSku().equals(entity.getSku())
                && repository.existsBySkuAndIdNot(dto.getSku(), id)) {
            throw new BusinessException("variantStock.skuExists", dto.getSku());
        }

        mapper.updateEntityFromDto(dto, entity);
        entity.setId(id);
        return mapper.toDto(repository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("variantStock.notFound", id);
        }
        repository.deleteById(id);
    }

    @Override
    public VariantStockDTO getById(Integer id) {
        return mapper.toDto(repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("variantStock.notFound", id)));
    }

    @Override
    public List<VariantStockDTO> getAll() {
        return mapper.toDtoList(repository.findAll());
    }

    @Override
    public List<VariantStockDTO> getByVariantId(Integer variantId) {
        if (!variantRepository.existsById(variantId)) {
            throw new ResourceNotFoundException("variantStock.variantNotFound", variantId);
        }
        return mapper.toDtoList(repository.findByVariantId(variantId));
    }

    @Override
    public Page<VariantStockDTO> searchVariantStocks(String keyword, Integer variantId, Pageable pageable) {
        return repository.searchVariantStocks(keyword, variantId, pageable).map(mapper::toDto);
    }
}

