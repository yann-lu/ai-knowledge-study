package com.memory.app.repository;

import com.memory.app.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    // 查找所有顶级分类
    List<Category> findByParentIsNullOrderBySortOrderAsc();
    
    // 查找指定父分类的所有子分类
    List<Category> findByParentIdOrderBySortOrderAsc(Long parentId);
    
    // 查找指定级别的所有分类
    List<Category> findByLevelOrderBySortOrderAsc(Integer level);
    
    // 查找分类路径（从根到指定分类）- 使用原生SQL查询
    @Query(value = "WITH RECURSIVE category_path(id, parent_id) AS (" +
           "  SELECT id, parent_id FROM category WHERE id = :id " +
           "  UNION ALL " +
           "  SELECT c.id, c.parent_id FROM category c " +
           "  INNER JOIN category_path cp ON c.id = cp.parent_id" +
           ") SELECT c.* FROM category c WHERE c.id = :id OR c.id IN (SELECT id FROM category_path)", 
           nativeQuery = true)
    List<Category> findCategoryPathById(Long id);
    
    // 按名称搜索分类
    List<Category> findByNameContainingIgnoreCaseOrderByLevelAscSortOrderAsc(String name);
} 