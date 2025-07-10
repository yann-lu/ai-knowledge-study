package com.memory.service;

import com.memory.model.Knowledge;
import com.memory.model.Category;
import java.util.List;

public interface KnowledgeService {
    List<Knowledge> findAll();
    List<Category> findAllCategories();
    List<Knowledge> findByCategoryIds(List<Long> categoryIds);
}