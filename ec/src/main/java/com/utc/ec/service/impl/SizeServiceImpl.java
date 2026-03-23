package com.utc.ec.service.impl;

import com.utc.ec.dto.SizeDTO;
import com.utc.ec.entity.Size;
import com.utc.ec.exception.BusinessException;
import com.utc.ec.exception.ResourceNotFoundException;
import com.utc.ec.mapper.SizeMapper;
import com.utc.ec.repository.SizeRepository;
import com.utc.ec.repository.VariantStockRepository;
import com.utc.ec.service.SizeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SizeServiceImpl implements SizeService {

    private final SizeRepository repository;
    private final VariantStockRepository variantStockRepository;
    private final SizeMapper mapper;

    @Override
    @Transactional
    public SizeDTO create(SizeDTO dto) {
        if (repository.existsByLabelAndType(dto.getLabel(), dto.getType())) {
            throw new BusinessException("size.labelTypeExists", dto.getLabel(), dto.getType());
        }
        Size entity = mapper.toEntity(dto);
        entity.setId(null);
        return mapper.toDto(repository.save(entity));
    }

    @Override
    @Transactional
    public SizeDTO update(Integer id, SizeDTO dto) {
        Size entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("size.notFound", id));

        if (dto.getLabel() != null && dto.getType() != null
                && repository.existsByLabelAndTypeAndIdNot(dto.getLabel(), dto.getType(), id)) {
            throw new BusinessException("size.labelTypeExists", dto.getLabel(), dto.getType());
        }

        mapper.updateEntityFromDto(dto, entity);
        entity.setId(id);
        return mapper.toDto(repository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("size.notFound", id);
        }
        if (variantStockRepository.existsBySizeId(id)) {
            throw new BusinessException("size.hasStocks");
        }
        repository.deleteById(id);
    }

    @Override
    public SizeDTO getById(Integer id) {
        return mapper.toDto(repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("size.notFound", id)));
    }

    @Override
    public List<SizeDTO> getAll() {
        return mapper.toDtoList(repository.findAll());
    }

    @Override
    public List<SizeDTO> getByType(String type) {
        return mapper.toDtoList(repository.findByTypeOrderBySortOrderAsc(type));
    }
}

