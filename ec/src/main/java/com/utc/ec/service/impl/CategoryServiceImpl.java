package com.utc.ec.service.impl;

import com.utc.ec.dto.CategoryDTO;
import com.utc.ec.entity.Category;
import com.utc.ec.exception.BusinessException;
import com.utc.ec.exception.ResourceNotFoundException;
import com.utc.ec.mapper.CategoryMapper;
import com.utc.ec.repository.CategoryRepository;
import com.utc.ec.repository.ProductRepository;
import com.utc.ec.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository repository;
    private final ProductRepository productRepository;
    private final CategoryMapper mapper;

    @Override
    @Transactional
    public CategoryDTO create(CategoryDTO dto) {
        if (dto.getParentId() != null && !repository.existsById(dto.getParentId())) {
            throw new ResourceNotFoundException("category.parentNotFound", dto.getParentId());
        }
        if (repository.existsBySlug(dto.getSlug())) {
            throw new BusinessException("category.slugExists", dto.getSlug());
        }
        if (repository.existsByNameAndParentId(dto.getName(), dto.getParentId())) {
            throw new BusinessException("category.nameExists", dto.getName());
        }

        Category entity = mapper.toEntity(dto);
        entity.setId(null);
        return mapper.toDto(repository.save(entity));
    }

    @Override
    @Transactional
    public CategoryDTO update(Integer id, CategoryDTO dto) {
        Category entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("category.notFound", id));

        if (dto.getParentId() != null && !dto.getParentId().equals(entity.getParentId())) {
            if (!repository.existsById(dto.getParentId())) {
                throw new ResourceNotFoundException("category.parentNotFound", dto.getParentId());
            }
            if (dto.getParentId().equals(id)) {
                throw new BusinessException("category.selfParent");
            }
        }
        if (dto.getSlug() != null && !dto.getSlug().equals(entity.getSlug())
                && repository.existsBySlugAndIdNot(dto.getSlug(), id)) {
            throw new BusinessException("category.slugExists", dto.getSlug());
        }

        mapper.updateEntityFromDto(dto, entity);
        entity.setId(id);
        return mapper.toDto(repository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("category.notFound", id);
        }
        if (!repository.findByParentId(id).isEmpty()) {
            throw new BusinessException("category.hasChildren");
        }
        if (productRepository.existsByCategoryId(id)) {
            throw new BusinessException("category.hasProducts");
        }
        repository.deleteById(id);
    }

    @Override
    public CategoryDTO getById(Integer id) {
        return mapper.toDto(repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("category.notFound", id)));
    }

    @Override
    public CategoryDTO getBySlug(String slug) {
        return mapper.toDto(repository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("category.notFound")));
    }

    @Override
    public List<CategoryDTO> getAll() {
        return mapper.toDtoList(repository.findAll());
    }

    @Override
    public List<CategoryDTO> getRootCategories() {
        return mapper.toDtoList(repository.findByParentIdIsNull());
    }

    @Override
    public List<CategoryDTO> getChildren(Integer parentId) {
        if (!repository.existsById(parentId)) {
            throw new ResourceNotFoundException("category.notFound", parentId);
        }
        return mapper.toDtoList(repository.findByParentId(parentId));
    }
}

