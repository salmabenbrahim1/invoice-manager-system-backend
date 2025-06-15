package com.example.backend.service;

import com.example.backend.dto.EngineDTO;
import com.example.backend.model.AISetting;
import com.example.backend.repository.AISettingRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

@Service
public class AISettingService {

    private final AISettingRepository repository;

    public AISettingService(AISettingRepository repository) {
        this.repository = repository;
    }

    public AISetting saveConfig(EngineDTO dto) {
        AISetting config = repository.findTopByOrderByIdDesc().orElse(new AISetting());
        config.setSelectedEngine(dto.getSelectedEngine());

        if ("gemini".equals(dto.getSelectedEngine())) {
            config.setGeminiApiKey(dto.getGeminiApiKey());
            config.setGeminiModelVersion(dto.getGeminiModelVersion());
            config.setDeepseekApiKey(null);
            config.setDeepseekEndpoint(null);
            config.setDeepseekModelVersion(null);

        } else if ("deepseek".equals(dto.getSelectedEngine())) {
            config.setDeepseekApiKey(dto.getDeepseekApiKey());
            config.setDeepseekEndpoint(dto.getDeepseekEndpoint());
            config.setDeepseekModelVersion(dto.getDeepseekModelVersion());
            config.setGeminiApiKey(null);
            config.setGeminiModelVersion(null);
        }

        return repository.save(config);
    }

    public AISetting getCurrentConfig() {
        return repository.findTopByOrderByIdDesc().orElse(null);
    }
}
