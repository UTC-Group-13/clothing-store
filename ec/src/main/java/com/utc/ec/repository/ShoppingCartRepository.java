package com.utc.ec.repository;

import com.utc.ec.entity.ShoppingCart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Integer> {

    Optional<ShoppingCart> findByUserId(Integer userId);
}
