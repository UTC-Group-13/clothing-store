package com.utc.ec.service.impl;

import com.utc.ec.dto.PaymentTypeDTO;
import com.utc.ec.entity.PaymentType;
import com.utc.ec.exception.BusinessException;
import com.utc.ec.exception.ResourceNotFoundException;
import com.utc.ec.repository.PaymentTypeRepository;
import com.utc.ec.repository.UserPaymentMethodRepository;
import com.utc.ec.service.PaymentTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentTypeServiceImpl implements PaymentTypeService {

    private final PaymentTypeRepository paymentTypeRepository;
    private final UserPaymentMethodRepository paymentMethodRepository;

    @Override
    public List<PaymentTypeDTO> getAll() {
        return paymentTypeRepository.findAll()
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public PaymentTypeDTO getById(Integer id) {
        PaymentType entity = paymentTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("payment.typeNotFound", id));
        return toDto(entity);
    }

    @Override
    @Transactional
    public PaymentTypeDTO create(PaymentTypeDTO dto) {
        PaymentType entity = new PaymentType();
        entity.setValue(dto.getValue());
        return toDto(paymentTypeRepository.save(entity));
    }

    @Override
    @Transactional
    public PaymentTypeDTO update(Integer id, PaymentTypeDTO dto) {
        PaymentType entity = paymentTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("payment.typeNotFound", id));
        entity.setValue(dto.getValue());
        return toDto(paymentTypeRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        if (!paymentTypeRepository.existsById(id)) {
            throw new ResourceNotFoundException("payment.typeNotFound", id);
        }
        // Kiem tra xem co user_payment_method nao dung loai nay khong
        if (paymentMethodRepository.findAll().stream()
                .anyMatch(pm -> id.equals(pm.getPaymentTypeId()))) {
            throw new BusinessException("payment.typeInUse");
        }
        paymentTypeRepository.deleteById(id);
    }

    // =========================================================

    private PaymentTypeDTO toDto(PaymentType entity) {
        PaymentTypeDTO dto = new PaymentTypeDTO();
        dto.setId(entity.getId());
        dto.setValue(entity.getValue());
        return dto;
    }
}

