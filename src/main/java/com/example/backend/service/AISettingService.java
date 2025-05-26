 package com.example.backend.service;
import com.example.backend.model.AISetting;
import com.example.backend.repository.AISettingRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AISettingService {

    private final AISettingRepository repository;

    public AISettingService(AISettingRepository repository) {
        this.repository = repository;
    }

    public String getCurrentEngine() {
        Optional<AISetting> setting = repository.findAll().stream().findFirst();
        return setting.map(AISetting::getCurrentEngine).orElse("gemini");
    }

    public void setCurrentEngine(String engine) {
        System.out.println("Setting engine to: " + engine);

        if (!engine.equals("gemini") && !engine.equals("deepseek")) {
            throw new IllegalArgumentException("Invalid engine: " + engine);
        }

        AISetting setting = repository.findAll().stream().findFirst().orElse(new AISetting());
        setting.setCurrentEngine(engine);
        repository.save(setting);
    }
}
