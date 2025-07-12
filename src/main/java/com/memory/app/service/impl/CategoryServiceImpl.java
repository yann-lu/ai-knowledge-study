package com.memory.app.service.impl;

import com.memory.app.dto.CategoryDTO;
import com.memory.app.model.Category;
import com.memory.app.model.Knowledge;
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
import java.util.Set;
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
    public List<CategoryDTO> getCategoryTree() {
        // 获取所有顶级分类（parent为null）
        List<Category> topCategories = categoryRepository.findByParentIsNullOrderBySortOrderAsc();
        
        // 转换为DTO并递归构建子分类树
        return topCategories.stream()
                .map(this::buildCategoryDTOWithChildren)
                .collect(Collectors.toList());
    }
    
    private CategoryDTO buildCategoryDTOWithChildren(Category category) {
        CategoryDTO dto = new CategoryDTO(
                category.getId(),
                category.getName(),
                category.getParent() != null ? category.getParent().getId() : null,
                category.getLevel(),
                category.getSortOrder(),
                category.getCreateTime(),
                category.getUpdateTime()
        );
        
        // 获取并转换子分类
        List<Category> children = categoryRepository.findByParentIdOrderBySortOrderAsc(category.getId());
        List<CategoryDTO> childrenDTO = children.stream()
                .map(this::buildCategoryDTOWithChildren)
                .collect(Collectors.toList());
        dto.setChildren(childrenDTO);
        
        return dto;
    }

    @Override
    public Map<String, Object> getCategoryTreeMap() {
        List<CategoryDTO> categoryTree = getCategoryTree();
        return buildCategoryTreeMap(categoryTree);
    }

    private Map<String, Object> buildCategoryTreeMap(List<CategoryDTO> categories) {
        Map<String, Object> result = new HashMap<>();
        result.put("categories", categories.stream().map(category -> {
            Map<String, Object> node = new HashMap<>();
            node.put("id", category.getId());
            node.put("name", category.getName());
            node.put("level", category.getLevel());
            node.put("sortOrder", category.getSortOrder());
            
            // 获取知识点统计信息
            Optional<Category> categoryEntity = categoryRepository.findById(category.getId());
            if (categoryEntity.isPresent()) {
                Set<Knowledge> knowledgeSet = categoryEntity.get().getKnowledgeSet();
                node.put("knowledgeSet", knowledgeSet);
                node.put("knowledgeCount", knowledgeSet.size());
            } else {
                node.put("knowledgeSet", new ArrayList<>());
                node.put("knowledgeCount", 0);
            }
            
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
    
    @Override
    public CategoryDTO convertToCategoryDTO(Category category) {
        return new CategoryDTO(
                category.getId(),
                category.getName(),
                category.getParent() != null ? category.getParent().getId() : null,
                category.getLevel(),
                category.getSortOrder(),
                category.getCreateTime(),
                category.getUpdateTime()
        );
    }
    
    @Override
    public List<CategoryDTO> convertToCategoryDTOList(List<Category> categories) {
        return categories.stream()
                .map(this::convertToCategoryDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public Optional<CategoryDTO> findByIdAsDTO(Long id) {
        return findById(id).map(this::convertToCategoryDTO);
    }
    
    @Override
    public List<CategoryDTO> findAllAsDTO() {
        return convertToCategoryDTOList(findAll());
    }
    
    @Override
    public List<CategoryDTO> findAllTopLevelAsDTO() {
        return convertToCategoryDTOList(findAllTopLevel());
    }
    
    @Override
    public List<CategoryDTO> findByParentIdAsDTO(Long parentId) {
        return convertToCategoryDTOList(findByParentId(parentId));
    }
}