 package com.example.backend.model;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "settings")
public class AISetting {
    @Id
    private String id;
    private String currentEngine;

    public AISetting() {}

    public AISetting(String currentEngine) {
        this.currentEngine = currentEngine;
    }

    public String getId() {
        return id;
    }

    public String getCurrentEngine() {
        return currentEngine;
    }

    public void setCurrentEngine(String currentEngine) {
        this.currentEngine = currentEngine;
    }
}
