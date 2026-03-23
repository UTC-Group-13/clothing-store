package com.utc.ec.repository;

import com.utc.ec.entity.ShopOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShopOrderRepository extends JpaRepository<ShopOrder, Integer> {

    boolean existsByPaymentMethodId(Integer paymentMethodId);

    List<ShopOrder> findByUserIdOrderByOrderDateDesc(Integer userId);

    Optional<ShopOrder> findByIdAndUserId(Integer id, Integer userId);

    Page<ShopOrder> findAllByOrderByOrderDateDesc(Pageable pageable);

    Page<ShopOrder> findByOrderStatusOrderByOrderDateDesc(Integer orderStatus, Pageable pageable);
}
