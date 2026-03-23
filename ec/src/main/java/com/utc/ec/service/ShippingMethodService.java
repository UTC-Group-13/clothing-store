package com.utc.ec.service;

import com.utc.ec.dto.ShippingMethodDTO;

import java.util.List;

public interface ShippingMethodService {

    List<ShippingMethodDTO> getAll();

    ShippingMethodDTO getById(Integer id);

    ShippingMethodDTO create(ShippingMethodDTO dto);

    ShippingMethodDTO update(Integer id, ShippingMethodDTO dto);

    void delete(Integer id);
}

