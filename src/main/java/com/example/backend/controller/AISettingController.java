package com.example.backend.controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.Map;



    @RestController
    @RequestMapping("/api/engine")
    public class AISettingController {

        private String selectedEngine = "gemini"; // Temporary in memory

        @GetMapping
        public ResponseEntity<Map<String, String>> getEngine() {
            return ResponseEntity.ok(Collections.singletonMap("engine", selectedEngine));
        }

        @PostMapping
        public ResponseEntity<Void> setEngine(@RequestBody Map<String, String> body) {
            String engine = body.get("engine");
            if (!"gemini".equals(engine) && !"deepseek".equals(engine)) {
                return ResponseEntity.badRequest().build();
            }
            this.selectedEngine = engine;
            return ResponseEntity.ok().build();
        }

        public String getSelectedEngine() {
            return selectedEngine;
        }
    }


