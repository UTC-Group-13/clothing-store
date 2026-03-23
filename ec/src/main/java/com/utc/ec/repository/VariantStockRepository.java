package com.utc.ec.repository;

import com.utc.ec.entity.VariantStock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VariantStockRepository extends JpaRepository<VariantStock, Integer> {

    List<VariantStock> findByVariantId(Integer variantId);

    boolean existsByVariantIdAndSizeId(Integer variantId, Integer sizeId);

    boolean existsByVariantIdAndSizeIdAndIdNot(Integer variantId, Integer sizeId, Integer id);

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, Integer id);

    boolean existsByVariantId(Integer variantId);

    boolean existsBySizeId(Integer sizeId);

    @Query("SELECT vs FROM VariantStock vs WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(vs.sku) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:variantId IS NULL OR vs.variantId = :variantId)")
    Page<VariantStock> searchVariantStocks(@Param("keyword") String keyword,
                                            @Param("variantId") Integer variantId,
                                            Pageable pageable);
}

