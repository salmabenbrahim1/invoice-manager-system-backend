package com.example.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class EmailValidationService {

    private final String accessKey = "7399b7c54f474fca947e860e1691166c";
    private final String apiUrl = "https://emailvalidation.abstractapi.com/v1/?api_key=%s&email=%s";

    public boolean isEmailValid(String email) {
        String url = String.format(apiUrl, accessKey, email);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            Map<String, Object> data = response.getBody();
            Map<String, Object> smtpCheck = (Map<String, Object>) data.get("is_smtp_valid");

            if (smtpCheck != null && smtpCheck.containsKey("value")) {
                return Boolean.TRUE.equals(smtpCheck.get("value"));
            }
        }

        return false;
    }
}