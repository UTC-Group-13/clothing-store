package com.utc.ec.repository;

import com.utc.ec.entity.ShoppingCartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShoppingCartItemRepository extends JpaRepository<ShoppingCartItem, Integer> {

    List<ShoppingCartItem> findByCartId(Integer cartId);

    Optional<ShoppingCartItem> findByCartIdAndVariantStockId(Integer cartId, Integer variantStockId);

    Optional<ShoppingCartItem> findByIdAndCartId(Integer id, Integer cartId);

    boolean existsByVariantStockId(Integer variantStockId);

    @Transactional
    void deleteByCartId(Integer cartId);
}
