package com.example.backend.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class EmailValidationService {

    private final String apiKey = "a40c3cebf93448a4b08fdeca3b92e381";
    private final String apiUrl = "https://emailvalidation.abstractapi.com/v1/?api_key=%s&email=%s";


    private final RestTemplate restTemplate = new RestTemplate();
    public boolean isEmailValid(String email) {
        try {
            String url = String.format(apiUrl, apiKey, email);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET, null, Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> data = response.getBody();

                String deliverability = (String) data.get("deliverability");

                System.out.println("API result: deliverability=" + deliverability);

                return "DELIVERABLE".equalsIgnoreCase(deliverability);
            } else {
                System.err.println("Incorrect API response.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
