package com.utc.ec.service.impl;

import com.utc.ec.dto.ShopBankAccountDTO;
import com.utc.ec.entity.ShopBankAccount;
import com.utc.ec.exception.ResourceNotFoundException;
import com.utc.ec.repository.ShopBankAccountRepository;
import com.utc.ec.service.ShopBankAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShopBankAccountServiceImpl implements ShopBankAccountService {

    private final ShopBankAccountRepository repository;

    @Override
    public List<ShopBankAccountDTO> getAll() {
        return repository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public ShopBankAccountDTO getActive() {
        return repository.findByIsActiveTrue()
                .map(this::toDto)
                .orElse(null);
    }

    @Override
    @Transactional
    public ShopBankAccountDTO create(ShopBankAccountDTO dto) {
        ShopBankAccount entity = new ShopBankAccount();
        entity.setBankId(dto.getBankId());
        entity.setBankName(dto.getBankName());
        entity.setAccountNumber(dto.getAccountNumber());
        entity.setAccountHolderName(dto.getAccountHolderName());
        entity.setLogoUrl(dto.getLogoUrl());

        // Nếu là tài khoản đầu tiên hoặc yêu cầu active → set active, bỏ active cái cũ
        boolean isFirst = repository.count() == 0;
        if (isFirst || Boolean.TRUE.equals(dto.getIsActive())) {
            deactivateAll();
            entity.setIsActive(true);
        } else {
            entity.setIsActive(false);
        }

        return toDto(repository.save(entity));
    }

    @Override
    @Transactional
    public ShopBankAccountDTO update(Integer id, ShopBankAccountDTO dto) {
        ShopBankAccount entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("shopBank.notFound", id));

        entity.setBankId(dto.getBankId());
        entity.setBankName(dto.getBankName());
        entity.setAccountNumber(dto.getAccountNumber());
        entity.setAccountHolderName(dto.getAccountHolderName());
        if (dto.getLogoUrl() != null) {
            entity.setLogoUrl(dto.getLogoUrl());
        }
        return toDto(repository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("shopBank.notFound", id);
        }
        repository.deleteById(id);
    }

    @Override
    @Transactional
    public ShopBankAccountDTO setActive(Integer id) {
        ShopBankAccount entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("shopBank.notFound", id));
        deactivateAll();
        entity.setIsActive(true);
        return toDto(repository.save(entity));
    }

    // =========================================================

    private void deactivateAll() {
        repository.findAll().forEach(a -> {
            if (Boolean.TRUE.equals(a.getIsActive())) {
                a.setIsActive(false);
                repository.save(a);
            }
        });
    }

    private ShopBankAccountDTO toDto(ShopBankAccount entity) {
        ShopBankAccountDTO dto = new ShopBankAccountDTO();
        dto.setId(entity.getId());
        dto.setBankId(entity.getBankId());
        dto.setBankName(entity.getBankName());
        dto.setAccountNumber(entity.getAccountNumber());
        dto.setAccountHolderName(entity.getAccountHolderName());
        dto.setLogoUrl(entity.getLogoUrl());
        dto.setIsActive(entity.getIsActive());
        return dto;
    }
}

