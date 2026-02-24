package com.utc.ec.repository;

import com.utc.ec.entity.PromotionCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PromotionCategoryRepository extends JpaRepository<PromotionCategory, Integer> {
}
