package com.example.webhookapp;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class BFHLApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(BFHLApplication.class, args);
    }

    @Override
    public void run(String... args) {
        RestTemplate restTemplate = new RestTemplate();

        // Step 1: Send POST to generateWebhook
        String registrationUrl = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("name", "Abhinav");
        requestBody.put("regNo", "1032222447");
        requestBody.put("email", "1032222447@mitwpu.edu.in");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(registrationUrl, request, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String webhookUrl = (String) response.getBody().get("webhook");
                String accessToken = (String) response.getBody().get("accessToken");

                // Step 2: Prepare SQL Query
                String finalSql = """
                        SELECT 
                            P.AMOUNT AS SALARY,
                            CONCAT(E.FIRST_NAME, ' ', E.LAST_NAME) AS NAME,
                            TIMESTAMPDIFF(YEAR, E.DOB, CURDATE()) AS AGE,
                            D.DEPARTMENT_NAME
                        FROM 
                            PAYMENTS P
                        JOIN 
                            EMPLOYEE E ON P.EMP_ID = E.EMP_ID
                        JOIN 
                            DEPARTMENT D ON E.DEPARTMENT = D.DEPARTMENT_ID
                        WHERE 
                            DAY(P.PAYMENT_TIME) != 1
                        ORDER BY 
                            P.AMOUNT DESC
                        LIMIT 1;
                        """;

                // Step 3: Submit final query to webhook
                HttpHeaders submitHeaders = new HttpHeaders();
                submitHeaders.setContentType(MediaType.APPLICATION_JSON);
                submitHeaders.setBearerAuth(accessToken);

                Map<String, String> finalPayload = new HashMap<>();
                finalPayload.put("finalQuery", finalSql);

                HttpEntity<Map<String, String>> submitRequest = new HttpEntity<>(finalPayload, submitHeaders);
                ResponseEntity<String> submitResponse = restTemplate.postForEntity(webhookUrl, submitRequest, String.class);

                System.out.println("Submission Status: " + submitResponse.getStatusCode());
                System.out.println("Response: " + submitResponse.getBody());
            } else {
                System.out.println("Failed to generate webhook");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
