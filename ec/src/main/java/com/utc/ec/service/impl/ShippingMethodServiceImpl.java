package com.utc.ec.service.impl;

import com.utc.ec.dto.ShippingMethodDTO;
import com.utc.ec.entity.ShippingMethod;
import com.utc.ec.exception.ResourceNotFoundException;
import com.utc.ec.repository.ShippingMethodRepository;
import com.utc.ec.service.ShippingMethodService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShippingMethodServiceImpl implements ShippingMethodService {

    private final ShippingMethodRepository repository;

    @Override
    public List<ShippingMethodDTO> getAll() {
        return repository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public ShippingMethodDTO getById(Integer id) {
        return toDto(repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("shipping.notFound", id)));
    }

    @Override
    @Transactional
    public ShippingMethodDTO create(ShippingMethodDTO dto) {
        ShippingMethod entity = new ShippingMethod();
        entity.setName(dto.getName());
        entity.setPrice(dto.getPrice());
        return toDto(repository.save(entity));
    }

    @Override
    @Transactional
    public ShippingMethodDTO update(Integer id, ShippingMethodDTO dto) {
        ShippingMethod entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("shipping.notFound", id));
        entity.setName(dto.getName());
        entity.setPrice(dto.getPrice());
        return toDto(repository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("shipping.notFound", id);
        }
        repository.deleteById(id);
    }

    private ShippingMethodDTO toDto(ShippingMethod entity) {
        ShippingMethodDTO dto = new ShippingMethodDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setPrice(entity.getPrice());
        return dto;
    }
}

