package com.utc.ec.repository;

import com.utc.ec.entity.Variation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VariationRepository extends JpaRepository<Variation, Integer> {
    List<Variation> findByCategoryId(Integer categoryId);

    // Kiểm tra tên thuộc tính đã tồn tại trong cùng 1 category chưa
    boolean existsByNameAndCategoryId(String name, Integer categoryId);

    @Query("SELECT v FROM Variation v WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(v.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:categoryId IS NULL OR v.categoryId = :categoryId)")
    Page<Variation> searchVariations(@Param("keyword") String keyword,
                                      @Param("categoryId") Integer categoryId,
                                      Pageable pageable);
}

