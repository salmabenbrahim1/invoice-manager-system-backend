package com.example.backend.controller;

import com.example.backend.service.AISettingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/engine")
public class AISettingController {

    private final AISettingService engineService;

    public AISettingController(AISettingService engineService) {
        this.engineService = engineService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")

    public ResponseEntity<Map<String, String>> getEngine() {
        String currentEngine = engineService.getCurrentEngine();
        return ResponseEntity.ok(Collections.singletonMap("engine", currentEngine));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Void> setEngine(@RequestBody Map<String, String> body) {
        {
            String engine = body.get("engine");
            try {
                engineService.setCurrentEngine(engine);
                return ResponseEntity.ok().build();
            } catch (IllegalArgumentException ex) {
                return ResponseEntity.badRequest().build();
            }
        }
    }
}
