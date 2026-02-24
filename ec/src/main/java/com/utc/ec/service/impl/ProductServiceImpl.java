package com.utc.ec.service.impl;

import com.utc.ec.dto.ProductDTO;
import com.utc.ec.entity.Product;
import com.utc.ec.repository.ProductRepository;
import com.utc.ec.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository repository;

    @Override
    public ProductDTO create(ProductDTO dto) {
        Product entity = new Product();
        BeanUtils.copyProperties(dto, entity);
        Product saved = repository.save(entity);
        ProductDTO result = new ProductDTO();
        BeanUtils.copyProperties(saved, result);
        return result;
    }

    @Override
    public ProductDTO update(Integer id, ProductDTO dto) {
        Product entity = repository.findById(id).orElseThrow();
        BeanUtils.copyProperties(dto, entity);
        entity.setId(id);
        Product saved = repository.save(entity);
        ProductDTO result = new ProductDTO();
        BeanUtils.copyProperties(saved, result);
        return result;
    }

    @Override
    public void delete(Integer id) {
        repository.deleteById(id);
    }

    @Override
    public ProductDTO getById(Integer id) {
        Product entity = repository.findById(id).orElseThrow();
        ProductDTO dto = new ProductDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    @Override
    public List<ProductDTO> getAll() {
        return repository.findAll().stream().map(entity -> {
            ProductDTO dto = new ProductDTO();
            BeanUtils.copyProperties(entity, dto);
            return dto;
        }).collect(Collectors.toList());
    }
}

