package com.utc.ec.service.impl;

import com.utc.ec.dto.VariationDTO;
import com.utc.ec.entity.Variation;
import com.utc.ec.mapper.VariationMapper;
import com.utc.ec.repository.VariationRepository;
import com.utc.ec.service.VariationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VariationServiceImpl implements VariationService {
    private final VariationRepository repository;
    private final VariationMapper mapper;

    @Override
    public VariationDTO create(VariationDTO dto) {
        Variation entity = mapper.toEntity(dto);
        Variation saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    public VariationDTO update(Integer id, VariationDTO dto) {
        Variation entity = repository.findById(id).orElseThrow();
        mapper.updateEntityFromDto(dto, entity);
        entity.setId(id);
        Variation saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    public void delete(Integer id) {
        repository.deleteById(id);
    }

    @Override
    public VariationDTO getById(Integer id) {
        Variation entity = repository.findById(id).orElseThrow();
        return mapper.toDto(entity);
    }

    @Override
    public List<VariationDTO> getAll() {
        return mapper.toDtoList(repository.findAll());
    }

    @Override
    public List<VariationDTO> getByCategoryId(Integer categoryId) {
        return mapper.toDtoList(repository.findByCategoryId(categoryId));
    }

    @Override
    public Page<VariationDTO> searchVariations(String keyword, Integer categoryId, Pageable pageable) {
        return repository.searchVariations(keyword, categoryId, pageable).map(mapper::toDto);
    }
}
