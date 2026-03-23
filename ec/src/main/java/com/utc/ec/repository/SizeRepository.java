package com.utc.ec.repository;

import com.utc.ec.entity.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SizeRepository extends JpaRepository<Size, Integer> {

    List<Size> findByType(String type);

    List<Size> findByTypeOrderBySortOrderAsc(String type);

    boolean existsByLabelAndType(String label, String type);

    boolean existsByLabelAndTypeAndIdNot(String label, String type, Integer id);
}

