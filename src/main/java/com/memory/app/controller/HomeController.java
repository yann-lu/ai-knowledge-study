package com.memory.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "redirect:/knowledge.html";
    }
    
    @GetMapping("/knowledge")
    public String knowledge() {
        return "redirect:/knowledge.html";
    }
    
    @GetMapping("/knowledge/new")
    public String newKnowledge() {
        return "redirect:/knowledge.html";
    }
    
    @GetMapping("/review")
    public String review() {
        return "redirect:/review.html";
    }
    
    @GetMapping("/category")
    public String category() {
        return "redirect:/category.html";
    }
    
    @GetMapping("/knowledge/{id}")
    public String knowledgeDetail(@PathVariable Long id) {
        return "redirect:/knowledge-detail.html?id=" + id;
    }
}