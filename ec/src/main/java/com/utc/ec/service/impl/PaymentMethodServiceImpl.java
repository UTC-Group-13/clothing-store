package com.utc.ec.service.impl;

import com.utc.ec.dto.UserPaymentMethodDTO;
import com.utc.ec.entity.SiteUser;
import com.utc.ec.entity.UserPaymentMethod;
import com.utc.ec.exception.BusinessException;
import com.utc.ec.exception.ResourceNotFoundException;
import com.utc.ec.repository.PaymentTypeRepository;
import com.utc.ec.repository.ShopOrderRepository;
import com.utc.ec.repository.SiteUserRepository;
import com.utc.ec.repository.UserPaymentMethodRepository;
import com.utc.ec.service.PaymentMethodService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentMethodServiceImpl implements PaymentMethodService {

    private final UserPaymentMethodRepository paymentMethodRepository;
    private final PaymentTypeRepository paymentTypeRepository;
    private final SiteUserRepository userRepository;
    private final ShopOrderRepository shopOrderRepository;

    @Override
    public List<UserPaymentMethodDTO> getMyPaymentMethods(String username) {
        SiteUser user = getUser(username);
        return paymentMethodRepository.findByUserId(user.getId())
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserPaymentMethodDTO addPaymentMethod(String username, UserPaymentMethodDTO dto) {
        SiteUser user = getUser(username);

        if (!paymentTypeRepository.existsById(dto.getPaymentTypeId())) {
            throw new ResourceNotFoundException("payment.typeNotFound", dto.getPaymentTypeId());
        }

        UserPaymentMethod entity = new UserPaymentMethod();
        entity.setUserId(user.getId());
        entity.setPaymentTypeId(dto.getPaymentTypeId());
        entity.setProvider(dto.getProvider());
        entity.setAccountNumber(dto.getAccountNumber());
        entity.setExpiryDate(dto.getExpiryDate());

        // Neu la payment method dau tien thi set lam mac dinh
        List<UserPaymentMethod> existing = paymentMethodRepository.findByUserId(user.getId());
        boolean isFirstOne = existing.isEmpty();
        entity.setIsDefault(isFirstOne ? 1 : 0);

        return toDto(paymentMethodRepository.save(entity));
    }

    @Override
    @Transactional
    public UserPaymentMethodDTO updatePaymentMethod(String username, Integer id, UserPaymentMethodDTO dto) {
        SiteUser user = getUser(username);

        UserPaymentMethod entity = paymentMethodRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("payment.methodNotFound", id));

        if (!paymentTypeRepository.existsById(dto.getPaymentTypeId())) {
            throw new ResourceNotFoundException("payment.typeNotFound", dto.getPaymentTypeId());
        }

        entity.setPaymentTypeId(dto.getPaymentTypeId());
        entity.setProvider(dto.getProvider());
        entity.setAccountNumber(dto.getAccountNumber());
        entity.setExpiryDate(dto.getExpiryDate());

        return toDto(paymentMethodRepository.save(entity));
    }

    @Override
    @Transactional
    public void deletePaymentMethod(String username, Integer id) {
        SiteUser user = getUser(username);

        UserPaymentMethod entity = paymentMethodRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("payment.methodNotFound", id));

        // Kiem tra xem co don hang nao dung phuong thuc nay khong
        if (shopOrderRepository.existsByPaymentMethodId(id)) {
            throw new BusinessException("payment.methodInUse");
        }

        paymentMethodRepository.delete(entity);

        // Neu xoa mac dinh thi tu dong set mac dinh moi (neu con payment method khac)
        if (entity.getIsDefault() != null && entity.getIsDefault() == 1) {
            paymentMethodRepository.findByUserId(user.getId())
                    .stream().findFirst()
                    .ifPresent(first -> {
                        first.setIsDefault(1);
                        paymentMethodRepository.save(first);
                    });
        }
    }

    @Override
    @Transactional
    public UserPaymentMethodDTO setDefault(String username, Integer id) {
        SiteUser user = getUser(username);

        UserPaymentMethod target = paymentMethodRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("payment.methodNotFound", id));

        // Bỏ mac dinh hien tai
        paymentMethodRepository.findByUserIdAndIsDefault(user.getId(), 1)
                .ifPresent(current -> {
                    if (!current.getId().equals(id)) {
                        current.setIsDefault(0);
                        paymentMethodRepository.save(current);
                    }
                });

        target.setIsDefault(1);
        return toDto(paymentMethodRepository.save(target));
    }

    // =========================================================
    //  Private helpers
    // =========================================================

    private SiteUser getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("auth.user.notFound"));
    }

    private UserPaymentMethodDTO toDto(UserPaymentMethod entity) {
        UserPaymentMethodDTO dto = new UserPaymentMethodDTO();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUserId());
        dto.setPaymentTypeId(entity.getPaymentTypeId());
        dto.setProvider(entity.getProvider());
        dto.setAccountNumber(entity.getAccountNumber());
        dto.setExpiryDate(entity.getExpiryDate());
        dto.setIsDefault(entity.getIsDefault());
        return dto;
    }
}

