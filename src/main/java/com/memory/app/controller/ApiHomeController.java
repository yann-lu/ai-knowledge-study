package com.memory.app.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ApiHomeController {

    private static final Logger logger = LoggerFactory.getLogger(ApiHomeController.class);

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        logger.info("Received request for /api/status");
        Map<String, Object> status = Map.of(
            "status", "ok",
            "version", "1.0.0",
            "apiName", "Ebbinghaus Memory API"
        );
        logger.info("Returning status: {}", status);
        return ResponseEntity.ok(status);
    }
}