package com.utc.ec.service.impl;

import com.utc.ec.dto.ProductCategoryDTO;
import com.utc.ec.entity.ProductCategory;
import com.utc.ec.exception.BusinessException;
import com.utc.ec.exception.ResourceNotFoundException;
import com.utc.ec.repository.ProductCategoryRepository;
import com.utc.ec.repository.ProductRepository;
import com.utc.ec.service.ProductCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductCategoryServiceImpl implements ProductCategoryService {

    private final ProductCategoryRepository repository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public ProductCategoryDTO create(ProductCategoryDTO dto) {
        // Validate parent tồn tại nếu có
        if (dto.getParentCategoryId() != null) {
            if (!repository.existsById(dto.getParentCategoryId())) {
                throw new ResourceNotFoundException("productCategory.parentNotFound", dto.getParentCategoryId());
            }
        }
        // Validate tên không trùng trong cùng cấp
        if (repository.existsByCategoryNameAndParentCategoryId(dto.getCategoryName(), dto.getParentCategoryId())) {
            throw new BusinessException("productCategory.nameExists", dto.getCategoryName());
        }

        ProductCategory entity = new ProductCategory();
        BeanUtils.copyProperties(dto, entity);
        entity.setId(null);
        return toDto(repository.save(entity));
    }

    @Override
    @Transactional
    public ProductCategoryDTO update(Integer id, ProductCategoryDTO dto) {
        ProductCategory entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("productCategory.notFound", id));

        // Validate parent tồn tại nếu thay đổi
        if (dto.getParentCategoryId() != null && !dto.getParentCategoryId().equals(entity.getParentCategoryId())) {
            if (!repository.existsById(dto.getParentCategoryId())) {
                throw new ResourceNotFoundException("productCategory.parentNotFound", dto.getParentCategoryId());
            }
        }
        // Validate tên không trùng trong cùng cấp (bỏ qua chính nó)
        Integer parentId = dto.getParentCategoryId() != null ? dto.getParentCategoryId() : entity.getParentCategoryId();
        if (!dto.getCategoryName().equals(entity.getCategoryName())
                && repository.existsByCategoryNameAndParentCategoryId(dto.getCategoryName(), parentId)) {
            throw new BusinessException("productCategory.nameExists", dto.getCategoryName());
        }

        BeanUtils.copyProperties(dto, entity);
        entity.setId(id);
        return toDto(repository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("productCategory.notFound", id);
        }
        // Không xóa nếu có danh mục con
        if (!repository.findByParentCategoryId(id).isEmpty()) {
            throw new BusinessException("productCategory.hasChildren");
        }
        // Không xóa nếu có sản phẩm
        if (productRepository.existsByCategoryId(id)) {
            throw new BusinessException("productCategory.hasProducts");
        }
        repository.deleteById(id);
    }

    @Override
    public ProductCategoryDTO getById(Integer id) {
        return toDto(repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("productCategory.notFound", id)));
    }

    @Override
    public List<ProductCategoryDTO> getAll() {
        return repository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<ProductCategoryDTO> getRootCategories() {
        return repository.findByParentCategoryIdIsNull().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<ProductCategoryDTO> getChildren(Integer parentId) {
        if (!repository.existsById(parentId)) {
            throw new ResourceNotFoundException("productCategory.notFound", parentId);
        }
        return repository.findByParentCategoryId(parentId).stream().map(this::toDto).collect(Collectors.toList());
    }

    private ProductCategoryDTO toDto(ProductCategory entity) {
        ProductCategoryDTO dto = new ProductCategoryDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}

