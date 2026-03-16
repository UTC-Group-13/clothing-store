package com.utc.ec.service.impl;

import com.utc.ec.dto.ProductDTO;
import com.utc.ec.entity.Product;
import com.utc.ec.exception.BusinessException;
import com.utc.ec.exception.ResourceNotFoundException;
import com.utc.ec.repository.ProductCategoryRepository;
import com.utc.ec.repository.ProductItemRepository;
import com.utc.ec.repository.ProductRepository;
import com.utc.ec.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;
    private final ProductCategoryRepository categoryRepository;
    private final ProductItemRepository productItemRepository;

    @Override
    @Transactional
    public ProductDTO create(ProductDTO dto) {
        // Validate category tồn tại nếu có
        if (dto.getCategoryId() != null && !categoryRepository.existsById(dto.getCategoryId())) {
            throw new ResourceNotFoundException("product.categoryNotFound", dto.getCategoryId());
        }

        Product entity = new Product();
        BeanUtils.copyProperties(dto, entity);
        entity.setId(null);
        return toDto(repository.save(entity));
    }

    @Override
    @Transactional
    public ProductDTO update(Integer id, ProductDTO dto) {
        Product entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("product.notFound", id));

        // Validate category tồn tại nếu thay đổi
        if (dto.getCategoryId() != null && !dto.getCategoryId().equals(entity.getCategoryId())) {
            if (!categoryRepository.existsById(dto.getCategoryId())) {
                throw new ResourceNotFoundException("product.categoryNotFound", dto.getCategoryId());
            }
        }

        BeanUtils.copyProperties(dto, entity);
        entity.setId(id);
        return toDto(repository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("product.notFound", id);
        }
        // Không xóa nếu có product_item
        if (!productItemRepository.findByProductId(id).isEmpty()) {
            throw new BusinessException("product.hasItems");
        }
        repository.deleteById(id);
    }

    @Override
    public ProductDTO getById(Integer id) {
        return toDto(repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("product.notFound", id)));
    }

    @Override
    public List<ProductDTO> getAll() {
        return repository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<ProductDTO> getByCategoryId(Integer categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("product.categoryNotFound", categoryId);
        }
        return repository.findByCategoryId(categoryId).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public Page<ProductDTO> getAllPaged(Pageable pageable) {
        return repository.findAll(pageable).map(this::toDto);
    }

    @Override
    public Page<ProductDTO> searchProducts(String keyword, Integer categoryId, Pageable pageable) {
        return repository.searchProducts(keyword, categoryId, pageable).map(this::toDto);
    }

    private ProductDTO toDto(Product entity) {
        ProductDTO dto = new ProductDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}
