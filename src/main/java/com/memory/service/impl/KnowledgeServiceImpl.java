package com.memory.service.impl;

import com.memory.model.Category;
import com.memory.model.Knowledge;
import com.memory.repository.CategoryRepository;
import com.memory.repository.KnowledgeRepository;
import com.memory.service.KnowledgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KnowledgeServiceImpl implements KnowledgeService {

    private final KnowledgeRepository knowledgeRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public List<Knowledge> findAll() {
        return knowledgeRepository.findAll();
    }

    @Override
    public List<Category> findAllCategories() {
        return categoryRepository.findByParentIsNullOrderBySortOrder();
    }

    @Override
    public List<Knowledge> findByCategoryIds(List<Long> categoryIds) {
        return knowledgeRepository.findByCategoryIdIn(categoryIds);
    }
}