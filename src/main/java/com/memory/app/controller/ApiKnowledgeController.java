package com.memory.app.controller;

import com.memory.app.model.Knowledge;
import com.memory.app.service.KnowledgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ApiKnowledgeController {

    private final KnowledgeService knowledgeService;

    @GetMapping
    public ResponseEntity<List<Knowledge>> getAllKnowledge() {
        List<Knowledge> knowledgeList = knowledgeService.findAll();
        return ResponseEntity.ok(knowledgeList);
    }
}