package com.utc.ec.service;

import com.utc.ec.dto.ShopBankAccountDTO;

import java.util.List;

public interface ShopBankAccountService {

    List<ShopBankAccountDTO> getAll();

    ShopBankAccountDTO getActive();

    ShopBankAccountDTO create(ShopBankAccountDTO dto);

    ShopBankAccountDTO update(Integer id, ShopBankAccountDTO dto);

    void delete(Integer id);

    ShopBankAccountDTO setActive(Integer id);
}

