package com.utc.ec.repository;

import com.utc.ec.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    List<Category> findByParentIdIsNull();

    List<Category> findByParentId(Integer parentId);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Integer id);

    boolean existsByNameAndParentId(String name, Integer parentId);

    Optional<Category> findBySlug(String slug);
}

