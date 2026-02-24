package com.utc.ec.repository;

import com.utc.ec.entity.VariationOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VariationOptionRepository extends JpaRepository<VariationOption, Integer> {
}

