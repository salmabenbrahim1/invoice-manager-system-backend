package com.example.backend.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "settings")
public class AISetting {
    @Id
    private String id;

    private String selectedEngine;

    private String geminiApiKey;
    private String geminiModelVersion;

    private String deepseekApiKey;
    private String deepseekEndpoint;
    private String deepseekModelVersion;

}

