package com.example.customersystem.service;

import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class EmailService {

    public void sendOtpEmail(String email, String otp) {
        // ดึง API Key จาก Environment Variable
        String apiKey = System.getenv("BREVO_API_KEY"); 
        
        System.out.println("Trying to send OTP to: " + email);
        
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("API Key is missing! Check BREVO_API_KEY environment variable.");
            return; 
        }

        String jsonBody = "{"
                + "\"sender\":{\"name\":\"GSB Portal\",\"email\":\"sskg82760@gmail.com\"},"
                + "\"to\":[{\"email\":\"" + email + "\"}],"
                + "\"subject\":\"Your OTP Code: " + otp + "\","
                + "\"htmlContent\":\"<html><body><h3>OTP Code: <b style='color:blue;'>" + otp + "</b></h3></body></html>\""
                + "}";

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
                    .header("api-key", apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            // log ผลลัพธ์เอาไว้เช็ค
            System.out.println("Status: " + response.statusCode());
            
            if (response.statusCode() >= 400) {
                System.err.println("Brevo Error: " + response.body());
            } else {
                System.out.println("Email sent successfully!");
            }
        } catch (Exception e) {
            System.err.println("Error sending email: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
