package com.utc.ec.repository;

import com.utc.ec.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Integer> {

    List<ProductVariant> findByProductId(Integer productId);

    boolean existsByProductIdAndColorId(Integer productId, Integer colorId);

    boolean existsByProductIdAndColorIdAndIdNot(Integer productId, Integer colorId, Integer id);

    boolean existsByProductId(Integer productId);

    boolean existsByColorId(Integer colorId);

    Optional<ProductVariant> findByProductIdAndIsDefaultTrue(Integer productId);

    /** Batch: lấy tất cả default variants cho danh sách product IDs */
    List<ProductVariant> findByProductIdInAndIsDefaultTrue(List<Integer> productIds);

    /** Fallback: lấy variant đầu tiên (theo id nhỏ nhất) nếu không có default */
    Optional<ProductVariant> findFirstByProductIdOrderByIdAsc(Integer productId);
}

