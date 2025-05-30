package com.example.backend.dto;

import lombok.Data;

@Data
public class EngineResponseDTO {
    private String selectedEngine;

    // Gemini
    private String geminiModelVersion;
    private String geminiApiKey;

    // DeepSeek
    private String deepseekEndpoint;
    private String deepseekApiKey;
    private String deepseekModelVersion;


}
