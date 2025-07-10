package com.memory.app.controller;

import com.memory.app.model.Category;
import com.memory.app.model.Knowledge;
import com.memory.app.model.KnowledgeFile;
import com.memory.app.model.dto.KnowledgeDTO;
import com.memory.app.service.CategoryService;
import com.memory.app.service.KnowledgeService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*")
@Controller
@RequestMapping("/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private static final Logger logger = LoggerFactory.getLogger(KnowledgeController.class);

    private final KnowledgeService knowledgeService;
    private final CategoryService categoryService;
    
    // 递归展平分类树并添加缩进
    private void flattenCategories(List<Category> categories, int level, List<Map<String, Object>> result) {
        for (Category category : categories) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", category.getId());
            item.put("indentName", " ".repeat(level * 4) + category.getName());
            result.add(item);
            
            if (category.getChildren() != null && !category.getChildren().isEmpty()) {
                flattenCategories(category.getChildren(), level + 1, result);
            }
        }
    }

    @GetMapping
    public String listAll(Model model, @RequestParam(required = false) Long categoryId) {
        List<Knowledge> knowledgeList;
        
        if (categoryId != null) {
            knowledgeList = knowledgeService.findByCategory(categoryId);
            model.addAttribute("selectedCategory", categoryService.findById(categoryId).orElse(null));
        } else {
            knowledgeList = knowledgeService.findAll();
        }
        
        // 将KnowledgeService添加到模型中，以便在模板中使用
        model.addAttribute("knowledgeService", knowledgeService);
        
        model.addAttribute("knowledgeList", knowledgeList);
        // 获取分类树并展平为带缩进的列表
        List<Category> categoryTree = categoryService.getCategoryTree();
        List<Map<String, Object>> allCategories = new ArrayList<>();
        flattenCategories(categoryTree, 0, allCategories);
        model.addAttribute("allCategories", allCategories);
        return "knowledge/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("knowledge", new KnowledgeDTO());
        model.addAttribute("allCategories", categoryService.findAll());
        return "knowledge/form";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Knowledge knowledge = knowledgeService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("知识点不存在: " + id));
        
        KnowledgeDTO dto = new KnowledgeDTO();
        dto.setId(knowledge.getId());
        dto.setTitle(knowledge.getTitle());
        dto.setContent(knowledge.getContent());
        dto.setOutline(knowledge.getOutline());
        
        // 设置分类ID
        if (knowledge.getCategories() != null) {
            dto.setCategoryIds(knowledge.getCategories().stream()
                    .map(Category::getId)
                    .collect(Collectors.toSet()));
        }
        
        model.addAttribute("knowledge", dto);
        model.addAttribute("allCategories", categoryService.findAll());
        return "knowledge/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute KnowledgeDTO knowledgeDTO) {
        logger.info("收到保存知识点请求: {}", knowledgeDTO);
        knowledgeService.save(knowledgeDTO);
        logger.info("知识点保存成功: {}", knowledgeDTO.getTitle());
        return "redirect:/knowledge";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        Knowledge knowledge = knowledgeService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("知识点不存在: " + id));
        model.addAttribute("knowledge", knowledge);
        model.addAttribute("allCategories", categoryService.findAll());
        return "knowledge/view";
    }
    
    @PostMapping("/generate-outline")
    @ResponseBody
    public String generateOutline(@RequestBody Map<String, String> request) {
        String content = request.get("content");
        logger.info("Received request to generate outline for content: {}", content.length() > 100 ? content.substring(0, 100) + "..." : content);
        return knowledgeService.generateOutline(content);
    }
    
    /**
     * 显示文件上传表单
     */
    @GetMapping("/upload")
    public String showUploadForm(Model model) {
        model.addAttribute("knowledgeFile", new KnowledgeFile());
        model.addAttribute("allCategories", categoryService.findAll());
        return "knowledge/upload";
    }
    
    /**
     * 处理文件上传
     */
    @PostMapping("/upload")
    public String uploadFile(@ModelAttribute KnowledgeFile knowledgeFile,
                            RedirectAttributes redirectAttributes) {
        try {
            // 验证文件是否已选择
            if (knowledgeFile.isFileEmpty()) {
                redirectAttributes.addFlashAttribute("error", "请选择文件");
                return "redirect:/knowledge/upload";
            }
            
            // 从文件创建知识点
            Knowledge knowledge = knowledgeService.createFromFile(knowledgeFile);
            redirectAttributes.addFlashAttribute("success", 
                    "文件上传成功，已创建知识点：" + knowledge.getTitle());
            
            return "redirect:/knowledge/" + knowledge.getId();
        } catch (IllegalArgumentException e) {
            // 处理参数错误
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/knowledge/upload";
        } catch (Exception e) {
            // 处理其他错误
            redirectAttributes.addFlashAttribute("error", "文件处理失败: " + e.getMessage());
            return "redirect:/knowledge/upload";
        }
    }
    
    @PostMapping("/{id}/categories")
    @ResponseBody
    public ResponseEntity<?> updateCategories(@PathVariable Long id, @RequestBody Set<Long> categoryIds) {
        try {
            Knowledge knowledge = knowledgeService.addCategories(id, categoryIds);
            return ResponseEntity.ok().body(knowledge);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("更新分类失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/by-categories")
    public String findByCategories(@RequestParam(required = false) Set<Long> categoryIds, Model model) {
        List<Knowledge> knowledgeList;
        
        if (categoryIds != null && !categoryIds.isEmpty()) {
            knowledgeList = knowledgeService.findByCategories(categoryIds);
            model.addAttribute("selectedCategoryIds", categoryIds);
        } else {
            knowledgeList = knowledgeService.findAll();
            model.addAttribute("selectedCategoryIds", new HashSet<Long>());
        }
        
        model.addAttribute("knowledgeList", knowledgeList);
        // 获取分类树并展平为带缩进的列表
        List<Category> categoryTree = categoryService.getCategoryTree();
        List<Map<String, Object>> allCategories = new ArrayList<>();
        flattenCategories(categoryTree, 0, allCategories);
        model.addAttribute("allCategories", allCategories);
        return "knowledge/list";
    }
}