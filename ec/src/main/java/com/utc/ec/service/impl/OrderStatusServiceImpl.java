package com.utc.ec.service.impl;

import com.utc.ec.dto.OrderStatusDTO;
import com.utc.ec.entity.OrderStatus;
import com.utc.ec.exception.BusinessException;
import com.utc.ec.exception.ResourceNotFoundException;
import com.utc.ec.repository.OrderStatusRepository;
import com.utc.ec.repository.ShopOrderRepository;
import com.utc.ec.service.OrderStatusService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderStatusServiceImpl implements OrderStatusService {

    // Cac trang thai mac dinh
    public static final String STATUS_PENDING    = "PENDING";
    public static final String STATUS_PROCESSING = "PROCESSING";
    public static final String STATUS_SHIPPED    = "SHIPPED";
    public static final String STATUS_DELIVERED  = "DELIVERED";
    public static final String STATUS_CANCELLED  = "CANCELLED";

    private final OrderStatusRepository repository;
    private final ShopOrderRepository shopOrderRepository;

    /**
     * Tu dong tao cac trang thai don hang mac dinh neu chua ton tai.
     * Boc trong try/catch de app khong crash neu DB chua san sang luc startup.
     */
    @PostConstruct
    public void initDefaultStatuses() {
        try {
            List<String> defaults = Arrays.asList(
                    STATUS_PENDING, STATUS_PROCESSING, STATUS_SHIPPED,
                    STATUS_DELIVERED, STATUS_CANCELLED);

            for (String statusName : defaults) {
                if (!repository.existsByStatus(statusName)) {
                    OrderStatus s = new OrderStatus();
                    s.setStatus(statusName);
                    repository.save(s);
                    log.info("[OrderStatus] Da tao trang thai mac dinh: {}", statusName);
                }
            }
            log.info("[OrderStatus] Khoi tao trang thai don hang hoan tat.");
        } catch (Exception e) {
            // Khong crash app neu DB chua san sang luc startup.
            // App van khoi dong binh thuong, cac trang thai se duoc tao sau.
            log.warn("[OrderStatus] Khong the khoi tao trang thai mac dinh luc startup " +
                     "(DB chua san sang?). App van tiep tuc khoi dong. Loi: {}", e.getMessage());
        }
    }

    @Override
    public List<OrderStatusDTO> getAll() {
        return repository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public OrderStatusDTO getById(Integer id) {
        return toDto(repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("order.statusNotFound", id)));
    }

    @Override
    @Transactional
    public OrderStatusDTO create(OrderStatusDTO dto) {
        if (repository.existsByStatus(dto.getStatus())) {
            throw new BusinessException("order.statusExists", dto.getStatus());
        }
        OrderStatus entity = new OrderStatus();
        entity.setStatus(dto.getStatus());
        return toDto(repository.save(entity));
    }

    @Override
    @Transactional
    public OrderStatusDTO update(Integer id, OrderStatusDTO dto) {
        OrderStatus entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("order.statusNotFound", id));
        entity.setStatus(dto.getStatus());
        return toDto(repository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("order.statusNotFound", id);
        }
        if (shopOrderRepository.findAll().stream()
                .anyMatch(o -> id.equals(o.getOrderStatus()))) {
            throw new BusinessException("order.statusInUse");
        }
        repository.deleteById(id);
    }

    private OrderStatusDTO toDto(OrderStatus entity) {
        OrderStatusDTO dto = new OrderStatusDTO();
        dto.setId(entity.getId());
        dto.setStatus(entity.getStatus());
        return dto;
    }
}

