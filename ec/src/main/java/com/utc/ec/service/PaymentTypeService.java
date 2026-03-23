package com.utc.ec.service;

import com.utc.ec.dto.PaymentTypeDTO;

import java.util.List;

public interface PaymentTypeService {

    List<PaymentTypeDTO> getAll();

    PaymentTypeDTO getById(Integer id);

    PaymentTypeDTO create(PaymentTypeDTO dto);

    PaymentTypeDTO update(Integer id, PaymentTypeDTO dto);

    void delete(Integer id);
}

