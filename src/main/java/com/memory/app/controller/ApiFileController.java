package com.memory.app.controller;

import com.memory.app.model.Knowledge;
import com.memory.app.model.KnowledgeFile;
import com.memory.app.service.KnowledgeService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ApiFileController {

    private static final Logger logger = LoggerFactory.getLogger(ApiFileController.class);
    
    private final KnowledgeService knowledgeService;
    
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "generateOutline", defaultValue = "true") boolean generateOutline,
            @RequestParam(value = "generateReviewPlan", defaultValue = "true") boolean generateReviewPlan) {
        
        try {
            // 验证文件是否已选择
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "请选择文件"));
            }
            
            // 创建KnowledgeFile对象
            KnowledgeFile knowledgeFile = new KnowledgeFile();
            knowledgeFile.setFile(file);
            knowledgeFile.setTitle(title);
            knowledgeFile.setGenerateOutline(generateOutline);
            knowledgeFile.setGenerateReviewPlan(generateReviewPlan);
            
            // 从文件创建知识点
            Knowledge knowledge = knowledgeService.createFromFile(knowledgeFile);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(knowledge);
        } catch (IllegalArgumentException e) {
            // 处理参数错误
            logger.error("文件上传参数错误: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            // 处理其他错误
            logger.error("文件处理失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "文件处理失败: " + e.getMessage()));
        }
    }
}