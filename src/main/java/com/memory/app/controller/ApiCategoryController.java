package com.memory.app.controller;

import com.memory.app.dto.CategoryDTO;
import com.memory.app.model.Category;
import com.memory.app.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ApiCategoryController {

    private static final Logger logger = LoggerFactory.getLogger(ApiCategoryController.class);
    
    private final CategoryService categoryService;
    
    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        List<CategoryDTO> categories = categoryService.findAllAsDTO();
        return ResponseEntity.ok(categories);
    }
    
    @GetMapping("/top-level")
    public ResponseEntity<List<CategoryDTO>> getTopLevelCategories() {
        List<CategoryDTO> categories = categoryService.findAllTopLevelAsDTO();
        return ResponseEntity.ok(categories);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getCategoryById(@PathVariable Long id) {
        try {
            return categoryService.findByIdAsDTO(id)
                    .map(categoryDTO -> ResponseEntity.ok().body((Object)categoryDTO))
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(Map.of("error", "分类不存在: " + id)));
        } catch (Exception e) {
            logger.error("获取分类失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "获取分类失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/children/{parentId}")
    public ResponseEntity<List<CategoryDTO>> getChildren(@PathVariable Long parentId) {
        List<CategoryDTO> children = categoryService.findByParentIdAsDTO(parentId);
        return ResponseEntity.ok(children);
    }
    
    @GetMapping("/tree")
    public ResponseEntity<Map<String, Object>> getCategoryTree() {
        Map<String, Object> tree = categoryService.getCategoryTreeMap();
        return ResponseEntity.ok(tree);
    }
    
    @PostMapping
    public ResponseEntity<?> createCategory(@RequestBody CategoryDTO categoryDTO) {
        try {
            logger.info("收到创建分类请求: {}", categoryDTO);
            Category category = new Category();
            category.setName(categoryDTO.getName());
            category.setSortOrder(categoryDTO.getSortOrder() != null ? categoryDTO.getSortOrder() : 0);
            
            if (categoryDTO.getParentId() != null) {
                categoryService.findById(categoryDTO.getParentId()).ifPresent(parent -> {
                    category.setParent(parent);
                    category.setLevel(parent.getLevel() + 1);
                });
            } else {
                category.setParent(null);
                category.setLevel(1);
            }
            
            Category savedCategory = categoryService.save(category);
            CategoryDTO savedCategoryDTO = categoryService.convertToCategoryDTO(savedCategory);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedCategoryDTO);
        } catch (Exception e) {
            logger.error("创建分类失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "创建分类失败: " + e.getMessage()));
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable Long id, @RequestBody CategoryDTO categoryDTO) {
        try {
            logger.info("收到更新分类请求: {}", categoryDTO);
            return categoryService.findById(id)
                    .map(category -> {
                        category.setName(categoryDTO.getName());
                        category.setSortOrder(categoryDTO.getSortOrder() != null ? categoryDTO.getSortOrder() : 0);
                        
                        if (categoryDTO.getParentId() != null) {
                            categoryService.findById(categoryDTO.getParentId()).ifPresent(parent -> {
                                category.setParent(parent);
                                category.setLevel(parent.getLevel() + 1);
                            });
                        } else {
                            category.setParent(null);
                            category.setLevel(1);
                        }
                        
                        Category updatedCategory = categoryService.save(category);
                        CategoryDTO updatedCategoryDTO = categoryService.convertToCategoryDTO(updatedCategory);
                        return ResponseEntity.ok().body((Object)updatedCategoryDTO);
                    })
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(Map.of("error", "分类不存在: " + id)));
        } catch (Exception e) {
            logger.error("更新分类失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "更新分类失败: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        try {
            logger.info("收到删除分类请求: {}", id);
            categoryService.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "分类删除成功"));
        } catch (Exception e) {
            logger.error("删除分类失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "删除分类失败: " + e.getMessage()));
        }
    }
}