package com.utc.ec.repository;

import com.utc.ec.entity.ShopBankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShopBankAccountRepository extends JpaRepository<ShopBankAccount, Integer> {

    Optional<ShopBankAccount> findByIsActiveTrue();
}

