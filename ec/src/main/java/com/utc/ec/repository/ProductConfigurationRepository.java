package com.utc.ec.repository;

import com.utc.ec.entity.ProductConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductConfigurationRepository extends JpaRepository<ProductConfiguration, Integer> {
}

