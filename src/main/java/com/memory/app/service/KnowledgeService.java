package com.memory.app.service;

import com.memory.app.model.Category;
import com.memory.app.model.Knowledge;
import com.memory.app.model.KnowledgeFile;
import com.memory.app.model.dto.KnowledgeDTO;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface KnowledgeService {
    Knowledge save(KnowledgeDTO knowledgeDTO);
    List<Knowledge> findAll();
    Optional<Knowledge> findById(Long id);
    String generateOutline(String content);
    
    /**
     * 从上传的文件中创建知识点
     * 
     * @param knowledgeFile 包含上传文件的KnowledgeFile对象
     * @return 创建的Knowledge对象
     * @throws Exception 如果文件解析失败
     */
    Knowledge createFromFile(KnowledgeFile knowledgeFile) throws Exception;
    
    // 根据分类ID查找知识点
    List<Knowledge> findByCategory(Long categoryId);
    
    // 根据多个分类ID查找知识点（满足所有分类）
    List<Knowledge> findByCategories(Set<Long> categoryIds);
    
    // 添加分类到知识点
    Knowledge addCategories(Long knowledgeId, Set<Long> categoryIds);
    
    // 从知识点中移除分类
    Knowledge removeCategories(Long knowledgeId, Set<Long> categoryIds);
    
    // 获取知识点的复习信息
    String getReviewedDates(Long knowledgeId);
    
    // 获取知识点的待复习日期
    String getUpcomingDates(Long knowledgeId);
} 