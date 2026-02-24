package com.utc.ec.service.impl;

import com.utc.ec.dto.ProductCategoryDTO;
import com.utc.ec.entity.ProductCategory;
import com.utc.ec.repository.ProductCategoryRepository;
import com.utc.ec.service.ProductCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductCategoryServiceImpl implements ProductCategoryService {
    private final ProductCategoryRepository repository;

    @Override
    public ProductCategoryDTO create(ProductCategoryDTO dto) {
        ProductCategory entity = new ProductCategory();
        BeanUtils.copyProperties(dto, entity);
        ProductCategory saved = repository.save(entity);
        ProductCategoryDTO result = new ProductCategoryDTO();
        BeanUtils.copyProperties(saved, result);
        return result;
    }

    @Override
    public ProductCategoryDTO update(Integer id, ProductCategoryDTO dto) {
        ProductCategory entity = repository.findById(id).orElseThrow();
        BeanUtils.copyProperties(dto, entity);
        entity.setId(id);
        ProductCategory saved = repository.save(entity);
        ProductCategoryDTO result = new ProductCategoryDTO();
        BeanUtils.copyProperties(saved, result);
        return result;
    }

    @Override
    public void delete(Integer id) {
        repository.deleteById(id);
    }

    @Override
    public ProductCategoryDTO getById(Integer id) {
        ProductCategory entity = repository.findById(id).orElseThrow();
        ProductCategoryDTO dto = new ProductCategoryDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    @Override
    public List<ProductCategoryDTO> getAll() {
        return repository.findAll().stream().map(entity -> {
            ProductCategoryDTO dto = new ProductCategoryDTO();
            BeanUtils.copyProperties(entity, dto);
            return dto;
        }).collect(Collectors.toList());
    }
}

