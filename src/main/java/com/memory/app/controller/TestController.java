package com.memory.app.controller;

import com.memory.app.util.MarkdownUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TestController {

    private static final Logger logger = LoggerFactory.getLogger(TestController.class);

    @PostMapping("/markdown")
    public ResponseEntity<?> testMarkdown(@RequestBody Map<String, String> request) {
        try {
            String content = request.get("content");
            logger.info("收到Markdown测试请求，内容长度: {}", content != null ? content.length() : 0);
            
            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "内容不能为空"));
            }
            
            String html = MarkdownUtils.convertToHtml(content);
            logger.info("Markdown解析完成，HTML长度: {}", html.length());
            
            return ResponseEntity.ok(Map.of(
                "html", html,
                "original", content
            ));
        } catch (Exception e) {
            logger.error("Markdown解析失败: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(Map.of("error", "解析失败: " + e.getMessage()));
        }
    }
}