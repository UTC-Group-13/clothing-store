package com.utc.ec.repository.spec;

import com.utc.ec.entity.Product;
import com.utc.ec.entity.ProductVariant;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

    private ProductSpecification() {
    }

    public static Specification<Product> withFilters(
            String name,
            List<Integer> categoryIds,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            List<Integer> colorIds,
            Boolean isActive) {

        return (Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Tìm theo tên sản phẩm
            if (name != null && !name.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")),
                        "%" + name.trim().toLowerCase() + "%"));
            }

            // Tìm theo nhiều danh mục
            if (categoryIds != null && !categoryIds.isEmpty()) {
                predicates.add(root.get("categoryId").in(categoryIds));
            }

            // Tìm theo giá tối thiểu
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("basePrice"), minPrice));
            }

            // Tìm theo giá tối đa
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("basePrice"), maxPrice));
            }

            // Trạng thái hoạt động
            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }

            // Tìm theo nhiều màu sắc (qua bảng product_variants)
            if (colorIds != null && !colorIds.isEmpty() && query != null) {
                Subquery<Integer> variantSubquery = query.subquery(Integer.class);
                Root<ProductVariant> variantRoot = variantSubquery.from(ProductVariant.class);
                variantSubquery.select(variantRoot.get("productId"))
                        .where(
                                cb.equal(variantRoot.get("productId"), root.get("id")),
                                variantRoot.get("colorId").in(colorIds)
                        );
                predicates.add(cb.exists(variantSubquery));
            }

            // Loại bỏ duplicate khi có join/subquery
            if (query != null) {
                query.distinct(true);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}


