package com.utc.ec.repository;

import com.utc.ec.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer>, JpaSpecificationExecutor<Product> {

    List<Product> findByCategoryId(Integer categoryId);

    List<Product> findByCategoryIdAndIsActiveTrue(Integer categoryId);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Integer id);

    boolean existsByCategoryId(Integer categoryId);

    Optional<Product> findBySlug(String slug);

    List<Product> findByIsActiveTrue();
}
