package com.utc.ec.repository;

import com.utc.ec.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    // Tìm kiếm sản phẩm theo keyword và/hoặc category (có phân trang)
    @Query("SELECT p FROM Product p WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:categoryId IS NULL OR p.categoryId = :categoryId)")
    Page<Product> searchProducts(@Param("keyword") String keyword,
                                  @Param("categoryId") Integer categoryId,
                                  Pageable pageable);

    // Lấy tất cả sản phẩm theo category
    List<Product> findByCategoryId(Integer categoryId);

    // Kiểm tra tên sản phẩm đã tồn tại chưa
    boolean existsByName(String name);

    // Kiểm tra category có sản phẩm không (dùng khi xóa category)
    boolean existsByCategoryId(Integer categoryId);
}
