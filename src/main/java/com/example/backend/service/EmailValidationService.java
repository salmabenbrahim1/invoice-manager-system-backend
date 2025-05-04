package com.example.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import java.util.Map;

@Service
public class EmailValidationService {

    private final String accessKey = "629249044ad56bc314691fafcf55e9d2";
    private final String apiUrl = "https://apilayer.net/api/check?access_key=%s&email=%s&smtp=1&format=1";

    public boolean isEmailValid(String email) {
        String url = String.format(apiUrl, accessKey, email);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            Map<String, Object> data = response.getBody();
            return Boolean.TRUE.equals(data.get("smtp_check"));
        }

        return false;
    }
}
