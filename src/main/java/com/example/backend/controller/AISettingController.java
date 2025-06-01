package com.example.backend.controller;

import com.example.backend.dto.EngineDTO;
import com.example.backend.dto.EngineResponseDTO;
import com.example.backend.model.AISetting;
import com.example.backend.service.AISettingService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

    @RestController
    @RequestMapping("/api/admin/engine")
    @CrossOrigin
    public class AISettingController {

        private final AISettingService service;

        public AISettingController(AISettingService service) {
            this.service = service;
        }

        @PostMapping("/config")
        public ResponseEntity<AISetting> saveEngineConfig(@RequestBody EngineDTO dto) {
            AISetting saved = service.saveConfig(dto);
            return ResponseEntity.ok(saved);
        }

        @GetMapping("")
        public ResponseEntity<?> redirectToConfig() {
            return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
                    .header(HttpHeaders.LOCATION, "/api/admin/engine/config")
                    .build();
        }

        @GetMapping("/config")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<EngineResponseDTO> getCurrentConfig() {
            AISetting config = service.getCurrentConfig();
            if (config == null) return ResponseEntity.notFound().build();

            EngineResponseDTO dto = new EngineResponseDTO();
            dto.setSelectedEngine(config.getSelectedEngine());
            dto.setGeminiModelVersion(config.getGeminiModelVersion());
            dto.setDeepseekEndpoint(config.getDeepseekEndpoint());
            dto.setDeepseekModelVersion(config.getDeepseekModelVersion());
            dto.setGeminiApiKey(config.getGeminiApiKey());
            dto.setDeepseekApiKey(config.getDeepseekApiKey());


            return ResponseEntity.ok(dto);
        }

    }
