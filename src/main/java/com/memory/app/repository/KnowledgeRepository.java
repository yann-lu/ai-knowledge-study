package com.memory.app.repository;

import com.memory.app.model.Knowledge;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface KnowledgeRepository extends JpaRepository<Knowledge, Long> {
    
    // 查找所有知识点，按创建时间倒序排列
    @Query("SELECT DISTINCT k FROM Knowledge k LEFT JOIN FETCH k.categories ORDER BY k.createTime DESC")
    List<Knowledge> findAllByOrderByCreateTimeDesc();
    
    // 根据分类ID查找知识点
    @Query("SELECT k FROM Knowledge k JOIN k.categories c WHERE c.id = :categoryId ORDER BY k.createTime DESC")
    List<Knowledge> findByCategoryId(Long categoryId);
    
    // 根据多个分类ID查找知识点（满足所有分类）
    @Query("SELECT k FROM Knowledge k WHERE SIZE(k.categories) >= :categoryCount AND k.id IN " +
           "(SELECT k2.id FROM Knowledge k2 JOIN k2.categories c WHERE c.id IN :categoryIds GROUP BY k2.id HAVING COUNT(DISTINCT c.id) = :categoryCount) " +
           "ORDER BY k.createTime DESC")
    List<Knowledge> findByCategoryIds(Set<Long> categoryIds, long categoryCount);
    
    // 根据关键词搜索知识点（标题或内容包含关键词）
    @Query("SELECT k FROM Knowledge k WHERE k.title LIKE %:keyword% OR k.content LIKE %:keyword% ORDER BY k.createTime DESC")
    List<Knowledge> findByKeyword(String keyword);
    
    // 根据分类和关键词搜索知识点
    @Query("SELECT k FROM Knowledge k JOIN k.categories c WHERE c.id = :categoryId AND (k.title LIKE %:keyword% OR k.content LIKE %:keyword%) ORDER BY k.createTime DESC")
    List<Knowledge> findByCategoryAndKeyword(Long categoryId, String keyword);
    
    // 根据多个分类和关键词搜索知识点
    @Query("SELECT k FROM Knowledge k WHERE SIZE(k.categories) >= :categoryCount AND k.id IN " +
           "(SELECT k2.id FROM Knowledge k2 JOIN k2.categories c WHERE c.id IN :categoryIds GROUP BY k2.id HAVING COUNT(DISTINCT c.id) = :categoryCount) " +
           "AND (k.title LIKE %:keyword% OR k.content LIKE %:keyword%) ORDER BY k.createTime DESC")
    List<Knowledge> findByCategoriesAndKeyword(Set<Long> categoryIds, long categoryCount, String keyword);
    
    // 分页查询所有知识点
    @Query("SELECT k FROM Knowledge k ORDER BY k.createTime DESC")
    Page<Knowledge> findAllByOrderByCreateTimeDesc(Pageable pageable);
    
    // 分页根据关键词搜索知识点
    @Query("SELECT k FROM Knowledge k WHERE k.title LIKE %:keyword% OR k.content LIKE %:keyword% ORDER BY k.createTime DESC")
    Page<Knowledge> findByKeyword(String keyword, Pageable pageable);
    
    // 分页根据分类查询知识点
    @Query("SELECT k FROM Knowledge k JOIN k.categories c WHERE c.id = :categoryId ORDER BY k.createTime DESC")
    Page<Knowledge> findByCategoryId(Long categoryId, Pageable pageable);
    
    // 分页根据分类和关键词搜索知识点
    @Query("SELECT k FROM Knowledge k JOIN k.categories c WHERE c.id = :categoryId AND (k.title LIKE %:keyword% OR k.content LIKE %:keyword%) ORDER BY k.createTime DESC")
    Page<Knowledge> findByCategoryAndKeyword(Long categoryId, String keyword, Pageable pageable);
    
    // 分页根据多个分类查询知识点
    @Query("SELECT k FROM Knowledge k WHERE SIZE(k.categories) >= :categoryCount AND k.id IN " +
           "(SELECT k2.id FROM Knowledge k2 JOIN k2.categories c WHERE c.id IN :categoryIds GROUP BY k2.id HAVING COUNT(DISTINCT c.id) = :categoryCount) " +
           "ORDER BY k.createTime DESC")
    Page<Knowledge> findByCategoryIds(Set<Long> categoryIds, long categoryCount, Pageable pageable);
    
    // 分页根据多个分类和关键词搜索知识点
    @Query("SELECT k FROM Knowledge k WHERE SIZE(k.categories) >= :categoryCount AND k.id IN " +
           "(SELECT k2.id FROM Knowledge k2 JOIN k2.categories c WHERE c.id IN :categoryIds GROUP BY k2.id HAVING COUNT(DISTINCT c.id) = :categoryCount) " +
           "AND (k.title LIKE %:keyword% OR k.content LIKE %:keyword%) ORDER BY k.createTime DESC")
    Page<Knowledge> findByCategoriesAndKeyword(Set<Long> categoryIds, long categoryCount, String keyword, Pageable pageable);
}