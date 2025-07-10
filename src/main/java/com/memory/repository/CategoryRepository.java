package com.memory.repository;

import com.memory.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByParentIsNullOrderBySortOrder();
    List<Category> findByParentIdOrderBySortOrder(Long parentId);
}