package com.memory.app.service.impl;

import com.memory.app.model.Category;
import com.memory.app.repository.CategoryRepository;
import com.memory.app.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public Category save(Category category) {
        // 设置分类级别
        if (category.getParent() != null) {
            category.setLevel(category.getParent().getLevel() + 1);
        } else {
            category.setLevel(1); // 顶级分类级别为1
        }
        
        return categoryRepository.save(category);
    }

    @Override
    public Optional<Category> findById(Long id) {
        return categoryRepository.findById(id);
    }

    @Override
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    @Override
    public List<Category> findAllTopLevel() {
        return categoryRepository.findByParentIsNullOrderBySortOrderAsc();
    }

    @Override
    public List<Category> findByParentId(Long parentId) {
        return categoryRepository.findByParentIdOrderBySortOrderAsc(parentId);
    }

    @Override
    public List<Category> getCategoryTree() {
        List<Category> allCategories = categoryRepository.findAll();
        
        // 按父子关系组织分类
        Map<Long, List<Category>> childrenMap = allCategories.stream()
                .filter(c -> c.getParent() != null)
                .collect(Collectors.groupingBy(c -> c.getParent().getId()));
        
        // 设置子分类
        allCategories.forEach(category -> {
            List<Category> children = childrenMap.get(category.getId());
            if (children != null) {
                category.setChildren(children);
            }
        });
        
        // 只返回顶级分类（包含其子分类）
        return allCategories.stream()
                .filter(c -> c.getParent() == null)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getCategoryTreeMap() {
        List<Category> categoryTree = getCategoryTree();
        return buildCategoryTreeMap(categoryTree);
    }

    private Map<String, Object> buildCategoryTreeMap(List<Category> categories) {
        Map<String, Object> result = new HashMap<>();
        result.put("categories", categories.stream().map(category -> {
            Map<String, Object> node = new HashMap<>();
            node.put("id", category.getId());
            node.put("name", category.getName());
            node.put("level", category.getLevel());
            node.put("sortOrder", category.getSortOrder());
            
            if (!category.getChildren().isEmpty()) {
                node.put("children", buildCategoryTreeMap(category.getChildren()).get("categories"));
            }
            
            return node;
        }).collect(Collectors.toList()));
        
        return result;
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        categoryRepository.deleteById(id);
    }

    @Override
    public List<Category> searchByName(String name) {
        return categoryRepository.findByNameContainingIgnoreCaseOrderByLevelAscSortOrderAsc(name);
    }

    @Override
    public List<Category> getCategoryPath(Long id) {
        return categoryRepository.findCategoryPathById(id);
    }
} 