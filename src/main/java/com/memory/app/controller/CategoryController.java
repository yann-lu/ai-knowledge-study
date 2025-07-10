package com.memory.app.controller;

import com.memory.app.model.Category;
import com.memory.app.model.dto.CategoryDTO;
import com.memory.app.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public String listAll(Model model) {
        List<Category> categories = categoryService.findAllTopLevel();
        model.addAttribute("categories", categories);
        return "category/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("category", new CategoryDTO());
        model.addAttribute("parentCategories", categoryService.findAll());
        return "category/form";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Category category = categoryService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("分类不存在: " + id));
        
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setLevel(category.getLevel());
        dto.setSortOrder(category.getSortOrder());
        if (category.getParent() != null) {
            dto.setParentId(category.getParent().getId());
        }
        
        model.addAttribute("category", dto);
        model.addAttribute("parentCategories", categoryService.findAll());
        return "category/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute CategoryDTO categoryDTO) {
        Category category = new Category();
        
        if (categoryDTO.getId() != null) {
            category = categoryService.findById(categoryDTO.getId())
                    .orElse(new Category());
        }
        
        category.setName(categoryDTO.getName());
        category.setSortOrder(categoryDTO.getSortOrder() != null ? categoryDTO.getSortOrder() : 0);
        
        final Category finalCategory = category;
        if (categoryDTO.getParentId() != null) {
            categoryService.findById(categoryDTO.getParentId()).ifPresent(parent -> {
                finalCategory.setParent(parent);
            });
        } else {
            finalCategory.setParent(null);
            finalCategory.setLevel(1);
        }
        
        categoryService.save(finalCategory);
        return "redirect:/category";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        Category category = categoryService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("分类不存在: " + id));
        model.addAttribute("category", category);
        
        // 查找子分类
        List<Category> children = categoryService.findByParentId(id);
        model.addAttribute("children", children);
        
        return "category/view";
    }
    
    @GetMapping("/tree")
    @ResponseBody
    public Map<String, Object> getCategoryTree() {
        return categoryService.getCategoryTreeMap();
    }
    
    @GetMapping("/children/{parentId}")
    @ResponseBody
    public List<Category> getChildren(@PathVariable Long parentId) {
        return categoryService.findByParentId(parentId);
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            categoryService.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("删除分类失败: " + e.getMessage());
        }
    }
}