package com.utc.ec.repository;

import com.utc.ec.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Integer> {

    // Lấy danh sách danh mục gốc (không có parent)
    List<ProductCategory> findByParentCategoryIdIsNull();

    // Lấy danh sách danh mục con theo parent
    List<ProductCategory> findByParentCategoryId(Integer parentCategoryId);

    // Kiểm tra tên danh mục đã tồn tại chưa (trong cùng 1 parent)
    boolean existsByCategoryNameAndParentCategoryId(String categoryName, Integer parentCategoryId);
}
