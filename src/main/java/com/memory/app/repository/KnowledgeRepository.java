package com.memory.app.repository;

import com.memory.app.model.Knowledge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface KnowledgeRepository extends JpaRepository<Knowledge, Long> {
    
    // 根据分类ID查找知识点
    @Query("SELECT k FROM Knowledge k JOIN k.categories c WHERE c.id = :categoryId")
    List<Knowledge> findByCategoryId(Long categoryId);
    
    // 根据多个分类ID查找知识点（满足所有分类）
    @Query("SELECT k FROM Knowledge k WHERE SIZE(k.categories) >= :categoryCount AND k.id IN " +
           "(SELECT k2.id FROM Knowledge k2 JOIN k2.categories c WHERE c.id IN :categoryIds GROUP BY k2.id HAVING COUNT(DISTINCT c.id) = :categoryCount)")
    List<Knowledge> findByCategoryIds(Set<Long> categoryIds, long categoryCount);
} 