package com.memory.app.service;

import com.memory.app.dto.CategoryDTO;
import com.memory.app.model.Category;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CategoryService {
    
    // 保存分类
    Category save(Category category);
    
    // 根据ID查找分类
    Optional<Category> findById(Long id);
    
    // 查找所有分类
    List<Category> findAll();
    
    // 查找所有顶级分类
    List<Category> findAllTopLevel();
    
    // 查找指定父分类的所有子分类
    List<Category> findByParentId(Long parentId);
    
    // 获取分类树（递归结构）
    List<CategoryDTO> getCategoryTree();
    
    // 获取分类树（Map结构，用于前端渲染）
    Map<String, Object> getCategoryTreeMap();
    
    // 删除分类
    void deleteById(Long id);
    
    // 按名称搜索分类
    List<Category> searchByName(String name);
    
    // 获取分类路径（从根到指定分类）
    List<Category> getCategoryPath(Long id);
    
    // 将Category转换为CategoryDTO
    CategoryDTO convertToCategoryDTO(Category category);
    
    // 将Category列表转换为CategoryDTO列表
    List<CategoryDTO> convertToCategoryDTOList(List<Category> categories);
    
    // 根据ID查找分类并返回DTO
    Optional<CategoryDTO> findByIdAsDTO(Long id);
    
    // 查找所有分类并返回DTO列表
    List<CategoryDTO> findAllAsDTO();
    
    // 查找所有顶级分类并返回DTO列表
    List<CategoryDTO> findAllTopLevelAsDTO();
    
    // 查找指定父分类的所有子分类并返回DTO列表
    List<CategoryDTO> findByParentIdAsDTO(Long parentId);
}