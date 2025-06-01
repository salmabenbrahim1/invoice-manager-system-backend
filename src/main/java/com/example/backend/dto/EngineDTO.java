package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EngineDTO {
    @NotBlank
        private String selectedEngine;

        // Gemini
        private String geminiApiKey;
        private String geminiModelVersion;

        // DeepSeek
        private String deepseekApiKey;
        private String deepseekEndpoint;
        private String deepseekModelVersion;
    }



