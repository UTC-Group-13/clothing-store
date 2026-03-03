package com.utc.ec.service.impl;

import com.utc.ec.dto.VariationOptionDTO;
import com.utc.ec.entity.VariationOption;
import com.utc.ec.mapper.VariationOptionMapper;
import com.utc.ec.repository.VariationOptionRepository;
import com.utc.ec.service.VariationOptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VariationOptionServiceImpl implements VariationOptionService {
    private final VariationOptionRepository repository;
    private final VariationOptionMapper mapper;

    @Override
    public VariationOptionDTO create(VariationOptionDTO dto) {
        VariationOption entity = mapper.toEntity(dto);
        VariationOption saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    public VariationOptionDTO update(Integer id, VariationOptionDTO dto) {
        VariationOption entity = repository.findById(id).orElseThrow();
        mapper.updateEntityFromDto(dto, entity);
        entity.setId(id);
        VariationOption saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    public void delete(Integer id) {
        repository.deleteById(id);
    }

    @Override
    public VariationOptionDTO getById(Integer id) {
        VariationOption entity = repository.findById(id).orElseThrow();
        return mapper.toDto(entity);
    }

    @Override
    public List<VariationOptionDTO> getAll() {
        return mapper.toDtoList(repository.findAll());
    }

    @Override
    public List<VariationOptionDTO> getByVariationId(Integer variationId) {
        return mapper.toDtoList(repository.findByVariationId(variationId));
    }

    @Override
    public Page<VariationOptionDTO> searchVariationOptions(String keyword, Integer variationId, Pageable pageable) {
        return repository.searchVariationOptions(keyword, variationId, pageable).map(mapper::toDto);
    }
}
