package com.utc.ec.service.impl;

import com.utc.ec.dto.ColorDTO;
import com.utc.ec.entity.Color;
import com.utc.ec.exception.BusinessException;
import com.utc.ec.exception.ResourceNotFoundException;
import com.utc.ec.mapper.ColorMapper;
import com.utc.ec.repository.ColorRepository;
import com.utc.ec.repository.ProductVariantRepository;
import com.utc.ec.service.ColorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ColorServiceImpl implements ColorService {

    private final ColorRepository repository;
    private final ProductVariantRepository variantRepository;
    private final ColorMapper mapper;

    @Override
    @Transactional
    public ColorDTO create(ColorDTO dto) {
        if (repository.existsBySlug(dto.getSlug())) {
            throw new BusinessException("color.slugExists", dto.getSlug());
        }
        Color entity = mapper.toEntity(dto);
        entity.setId(null);
        return mapper.toDto(repository.save(entity));
    }

    @Override
    @Transactional
    public ColorDTO update(Integer id, ColorDTO dto) {
        Color entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("color.notFound", id));

        if (dto.getSlug() != null && !dto.getSlug().equals(entity.getSlug())
                && repository.existsBySlugAndIdNot(dto.getSlug(), id)) {
            throw new BusinessException("color.slugExists", dto.getSlug());
        }

        mapper.updateEntityFromDto(dto, entity);
        entity.setId(id);
        return mapper.toDto(repository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("color.notFound", id);
        }
        if (variantRepository.existsByColorId(id)) {
            throw new BusinessException("color.hasVariants");
        }
        repository.deleteById(id);
    }

    @Override
    public ColorDTO getById(Integer id) {
        return mapper.toDto(repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("color.notFound", id)));
    }

    @Override
    public List<ColorDTO> getAll() {
        return mapper.toDtoList(repository.findAll());
    }
}

