package com.example.backend.service;


import org.springframework.stereotype.Service;

@Service
public class AISettingService {
    private String currentEngine = "gemini"; // Default engine

    public String getCurrentEngine() {
        return currentEngine;
    }

    public void setCurrentEngine(String engine) {
        if (engine.equals("gemini") || engine.equals("deepseek")) {
            this.currentEngine = engine;
        } else {
            throw new IllegalArgumentException("Invalid engine: " + engine);
        }
    }
}


