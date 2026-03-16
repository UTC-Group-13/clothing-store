package com.utc.ec.service.impl;

import com.utc.ec.dto.VariationDTO;
import com.utc.ec.entity.Variation;
import com.utc.ec.exception.BusinessException;
import com.utc.ec.exception.ResourceNotFoundException;
import com.utc.ec.mapper.VariationMapper;
import com.utc.ec.repository.ProductCategoryRepository;
import com.utc.ec.repository.VariationOptionRepository;
import com.utc.ec.repository.VariationRepository;
import com.utc.ec.service.VariationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VariationServiceImpl implements VariationService {

    private final VariationRepository repository;
    private final ProductCategoryRepository categoryRepository;
    private final VariationOptionRepository variationOptionRepository;
    private final VariationMapper mapper;

    @Override
    @Transactional
    public VariationDTO create(VariationDTO dto) {
        // Validate category tồn tại
        if (!categoryRepository.existsById(dto.getCategoryId())) {
            throw new ResourceNotFoundException("variation.categoryNotFound", dto.getCategoryId());
        }
        // Validate tên không trùng trong cùng category
        if (repository.existsByNameAndCategoryId(dto.getName(), dto.getCategoryId())) {
            throw new BusinessException("variation.nameExistsInCategory", dto.getName());
        }

        Variation entity = mapper.toEntity(dto);
        entity.setId(null);
        return mapper.toDto(repository.save(entity));
    }

    @Override
    @Transactional
    public VariationDTO update(Integer id, VariationDTO dto) {
        Variation entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("variation.notFound", id));

        // Validate category tồn tại nếu thay đổi
        if (dto.getCategoryId() != null && !dto.getCategoryId().equals(entity.getCategoryId())) {
            if (!categoryRepository.existsById(dto.getCategoryId())) {
                throw new ResourceNotFoundException("variation.categoryNotFound", dto.getCategoryId());
            }
        }
        // Validate tên không trùng trong cùng category (bỏ qua chính nó)
        Integer categoryId = dto.getCategoryId() != null ? dto.getCategoryId() : entity.getCategoryId();
        if (!dto.getName().equals(entity.getName())
                && repository.existsByNameAndCategoryId(dto.getName(), categoryId)) {
            throw new BusinessException("variation.nameExistsInCategory", dto.getName());
        }

        mapper.updateEntityFromDto(dto, entity);
        entity.setId(id);
        return mapper.toDto(repository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("variation.notFound", id);
        }
        // Không xóa nếu có variation_option đang dùng
        if (variationOptionRepository.existsByVariationId(id)) {
            throw new BusinessException("variation.hasOptions");
        }
        repository.deleteById(id);
    }

    @Override
    public VariationDTO getById(Integer id) {
        return mapper.toDto(repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("variation.notFound", id)));
    }

    @Override
    public List<VariationDTO> getAll() {
        return mapper.toDtoList(repository.findAll());
    }

    @Override
    public List<VariationDTO> getByCategoryId(Integer categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("variation.categoryNotFound", categoryId);
        }
        return mapper.toDtoList(repository.findByCategoryId(categoryId));
    }

    @Override
    public Page<VariationDTO> searchVariations(String keyword, Integer categoryId, Pageable pageable) {
        return repository.searchVariations(keyword, categoryId, pageable).map(mapper::toDto);
    }
}
