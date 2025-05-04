package com.example.backend.service;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

@Service
public class EmailValidationService {

    private final String API_KEY = "629249044ad56bc314691fafcf55e9d2";

    public boolean isEmailValid(String email) {
        try {
            String urlString = "http://apilayer.net/api/check?access_key=" + API_KEY + "&email=" + email + "&smtp=1&format=1";
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            InputStream responseStream = connection.getInputStream();
            Scanner scanner = new Scanner(responseStream).useDelimiter("\\A");
            String response = scanner.hasNext() ? scanner.next() : "";

            JSONObject jsonObject = new JSONObject(response);
            return jsonObject.getBoolean("smtp_check"); // true si l'email existe réellement
        } catch (Exception e) {
            e.printStackTrace();
            return false; // Par précaution
        }
    }
}
