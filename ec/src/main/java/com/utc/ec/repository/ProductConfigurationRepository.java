package com.utc.ec.repository;

import com.utc.ec.entity.ProductConfiguration;
import com.utc.ec.entity.ProductConfigurationId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductConfigurationRepository extends JpaRepository<ProductConfiguration, ProductConfigurationId> {
    List<ProductConfiguration> findByProductItemId(Integer productItemId);
    List<ProductConfiguration> findByVariationOptionId(Integer variationOptionId);

    // Kiểm tra có configuration nào dùng product_item này không
    boolean existsByProductItemId(Integer productItemId);

    // Kiểm tra có configuration nào dùng variation_option này không
    boolean existsByVariationOptionId(Integer variationOptionId);
}

