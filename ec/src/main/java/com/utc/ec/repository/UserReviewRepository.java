package com.utc.ec.repository;

import com.utc.ec.entity.UserReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserReviewRepository extends JpaRepository<UserReview, Integer> {

    boolean existsByUserIdAndOrderedProductId(Integer userId, Integer orderedProductId);

    List<UserReview> findByUserIdOrderByCreatedAtDesc(Integer userId);

    @Query("""
        SELECT ur FROM UserReview ur
        JOIN OrderLine ol ON ur.orderedProductId = ol.id
        JOIN VariantStock vs ON ol.variantStockId = vs.id
        JOIN ProductVariant pv ON vs.variantId = pv.id
        WHERE pv.productId = :productId
        ORDER BY ur.createdAt DESC
    """)
    List<UserReview> findByProductId(@Param("productId") Integer productId);

    @Query("""
        SELECT COUNT(ur) FROM UserReview ur
        JOIN OrderLine ol ON ur.orderedProductId = ol.id
        JOIN VariantStock vs ON ol.variantStockId = vs.id
        JOIN ProductVariant pv ON vs.variantId = pv.id
        WHERE pv.productId = :productId
    """)
    Integer countByProductId(@Param("productId") Integer productId);

    @Query("""
        SELECT AVG(ur.ratingValue) FROM UserReview ur
        JOIN OrderLine ol ON ur.orderedProductId = ol.id
        JOIN VariantStock vs ON ol.variantStockId = vs.id
        JOIN ProductVariant pv ON vs.variantId = pv.id
        WHERE pv.productId = :productId
    """)
    Double avgRatingByProductId(@Param("productId") Integer productId);
}
