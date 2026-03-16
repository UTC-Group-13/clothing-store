package com.utc.ec.service.impl;

import com.utc.ec.dto.VariationOptionDTO;
import com.utc.ec.entity.VariationOption;
import com.utc.ec.exception.BusinessException;
import com.utc.ec.exception.ResourceNotFoundException;
import com.utc.ec.mapper.VariationOptionMapper;
import com.utc.ec.repository.ProductConfigurationRepository;
import com.utc.ec.repository.VariationOptionRepository;
import com.utc.ec.repository.VariationRepository;
import com.utc.ec.service.VariationOptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VariationOptionServiceImpl implements VariationOptionService {

    private final VariationOptionRepository repository;
    private final VariationRepository variationRepository;
    private final ProductConfigurationRepository configurationRepository;
    private final VariationOptionMapper mapper;

    @Override
    @Transactional
    public VariationOptionDTO create(VariationOptionDTO dto) {
        // Validate variation tồn tại
        if (!variationRepository.existsById(dto.getVariationId())) {
            throw new ResourceNotFoundException("variationOption.variationNotFound", dto.getVariationId());
        }
        // Validate giá trị không trùng trong cùng variation
        if (repository.existsByVariationIdAndValue(dto.getVariationId(), dto.getValue())) {
            throw new BusinessException("variationOption.valueExists", dto.getValue());
        }

        VariationOption entity = mapper.toEntity(dto);
        entity.setId(null);
        return mapper.toDto(repository.save(entity));
    }

    @Override
    @Transactional
    public VariationOptionDTO update(Integer id, VariationOptionDTO dto) {
        VariationOption entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("variationOption.notFound", id));

        // Validate variation tồn tại nếu thay đổi
        if (dto.getVariationId() != null && !dto.getVariationId().equals(entity.getVariationId())) {
            if (!variationRepository.existsById(dto.getVariationId())) {
                throw new ResourceNotFoundException("variationOption.variationNotFound", dto.getVariationId());
            }
        }
        // Validate giá trị không trùng trong cùng variation (bỏ qua chính nó)
        Integer variationId = dto.getVariationId() != null ? dto.getVariationId() : entity.getVariationId();
        if (dto.getValue() != null && !dto.getValue().equals(entity.getValue())
                && repository.existsByVariationIdAndValue(variationId, dto.getValue())) {
            throw new BusinessException("variationOption.valueExists", dto.getValue());
        }

        mapper.updateEntityFromDto(dto, entity);
        entity.setId(id);
        return mapper.toDto(repository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("variationOption.notFound", id);
        }
        // Không xóa nếu đang được dùng trong product_configuration
        if (configurationRepository.existsByVariationOptionId(id)) {
            throw new BusinessException("variationOption.hasConfigurations");
        }
        repository.deleteById(id);
    }

    @Override
    public VariationOptionDTO getById(Integer id) {
        return mapper.toDto(repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("variationOption.notFound", id)));
    }

    @Override
    public List<VariationOptionDTO> getAll() {
        return mapper.toDtoList(repository.findAll());
    }

    @Override
    public List<VariationOptionDTO> getByVariationId(Integer variationId) {
        if (!variationRepository.existsById(variationId)) {
            throw new ResourceNotFoundException("variationOption.variationNotFound", variationId);
        }
        return mapper.toDtoList(repository.findByVariationId(variationId));
    }

    @Override
    public Page<VariationOptionDTO> searchVariationOptions(String keyword, Integer variationId, Pageable pageable) {
        return repository.searchVariationOptions(keyword, variationId, pageable).map(mapper::toDto);
    }
}
