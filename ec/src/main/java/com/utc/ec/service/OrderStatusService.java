package com.utc.ec.service;

import com.utc.ec.dto.OrderStatusDTO;

import java.util.List;

public interface OrderStatusService {

    List<OrderStatusDTO> getAll();

    OrderStatusDTO getById(Integer id);

    OrderStatusDTO create(OrderStatusDTO dto);

    OrderStatusDTO update(Integer id, OrderStatusDTO dto);

    void delete(Integer id);
}

