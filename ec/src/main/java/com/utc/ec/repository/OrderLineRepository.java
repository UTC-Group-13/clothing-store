package com.utc.ec.repository;

import com.utc.ec.entity.OrderLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderLineRepository extends JpaRepository<OrderLine, Integer> {

    List<OrderLine> findByOrderId(Integer orderId);

    boolean existsByVariantStockId(Integer variantStockId);
}
