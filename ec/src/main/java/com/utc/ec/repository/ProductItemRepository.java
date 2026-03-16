package com.utc.ec.repository;

import com.utc.ec.entity.ProductItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductItemRepository extends JpaRepository<ProductItem, Integer> {
    List<ProductItem> findByProductId(Integer productId);

    // Kiểm tra SKU đã tồn tại chưa
    boolean existsBySku(String sku);

    // Kiểm tra SKU đã tồn tại chưa (bỏ qua record hiện tại khi update)
    boolean existsBySkuAndIdNot(String sku, Integer id);

    @Query("SELECT pi FROM ProductItem pi WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(pi.sku) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:productId IS NULL OR pi.productId = :productId) AND " +
           "(:minPrice IS NULL OR pi.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR pi.price <= :maxPrice)")
    Page<ProductItem> searchProductItems(@Param("keyword") String keyword,
                                          @Param("productId") Integer productId,
                                          @Param("minPrice") Integer minPrice,
                                          @Param("maxPrice") Integer maxPrice,
                                          Pageable pageable);
}

