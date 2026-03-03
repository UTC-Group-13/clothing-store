package com.utc.ec.service.impl;

import com.utc.ec.dto.ProductItemDTO;
import com.utc.ec.entity.ProductItem;
import com.utc.ec.mapper.ProductItemMapper;
import com.utc.ec.repository.ProductItemRepository;
import com.utc.ec.service.ProductItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductItemServiceImpl implements ProductItemService {
    private final ProductItemRepository repository;
    private final ProductItemMapper mapper;

    @Override
    public ProductItemDTO create(ProductItemDTO dto) {
        ProductItem entity = mapper.toEntity(dto);
        ProductItem saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    public ProductItemDTO update(Integer id, ProductItemDTO dto) {
        ProductItem entity = repository.findById(id).orElseThrow();
        mapper.updateEntityFromDto(dto, entity);
        entity.setId(id);
        ProductItem saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    public void delete(Integer id) {
        repository.deleteById(id);
    }

    @Override
    public ProductItemDTO getById(Integer id) {
        ProductItem entity = repository.findById(id).orElseThrow();
        return mapper.toDto(entity);
    }

    @Override
    public List<ProductItemDTO> getAll() {
        return mapper.toDtoList(repository.findAll());
    }

    @Override
    public List<ProductItemDTO> getByProductId(Integer productId) {
        return mapper.toDtoList(repository.findByProductId(productId));
    }

    @Override
    public Page<ProductItemDTO> searchProductItems(String keyword, Integer productId, Integer minPrice, Integer maxPrice, Pageable pageable) {
        return repository.searchProductItems(keyword, productId, minPrice, maxPrice, pageable).map(mapper::toDto);
    }
}
