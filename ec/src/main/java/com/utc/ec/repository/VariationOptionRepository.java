package com.utc.ec.repository;

import com.utc.ec.entity.VariationOption;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VariationOptionRepository extends JpaRepository<VariationOption, Integer> {
    List<VariationOption> findByVariationId(Integer variationId);

    // Kiểm tra giá trị đã tồn tại trong cùng 1 variation chưa
    boolean existsByVariationIdAndValue(Integer variationId, String value);

    // Kiểm tra variation có đang có option nào không (dùng khi xóa variation)
    boolean existsByVariationId(Integer variationId);

    @Query("SELECT vo FROM VariationOption vo WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(vo.value) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:variationId IS NULL OR vo.variationId = :variationId)")
    Page<VariationOption> searchVariationOptions(@Param("keyword") String keyword,
                                                  @Param("variationId") Integer variationId,
                                                  Pageable pageable);
}

