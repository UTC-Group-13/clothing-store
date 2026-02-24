package com.utc.ec.repository;

import com.utc.ec.entity.Variation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VariationRepository extends JpaRepository<Variation, Integer> {
}

