package com.recruitment.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class EvaluationService {

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String apiUrl;

    // Retry config
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 2000;

    public EvaluationService(
        RestTemplate restTemplate,
        @Value("${together.api.key}") String apiKey,
        @Value("${spring.ai.openai.base-url}") String apiUrl
    ) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
    }

    public boolean isAnswerRelevant(String question, String answer) {
        String prompt = String.format("""
            You are an AI that checks if an answer is relevant to a question.
            Question: "%s"
            Answer: "%s"
            Reply with only "YES" if relevant, or "NO" if irrelevant.
        """, question, answer);

        Map<String, Object> body = new HashMap<>();
        body.put("model", "mistralai/Mistral-7B-Instruct-v0.2");
        body.put("temperature", 0.2);
        body.put("max_tokens", 10);
        body.put("messages", List.of(Map.of("role", "user", "content", prompt)));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        int attempt = 0;
        while (attempt < MAX_RETRIES) {
            try {
                ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    entity,
                    Map.class
                );

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    Object choicesObj = response.getBody().get("choices");
                    if (choicesObj instanceof List<?> choices && !choices.isEmpty()) {
                        Object firstChoice = choices.get(0);
                        if (firstChoice instanceof Map<?, ?> choiceMap) {
                            Object messageObj = choiceMap.get("message");
                            if (messageObj instanceof Map<?, ?> messageMap) {
                                Object contentObj = messageMap.get("content");
                                if (contentObj instanceof String result) {
                                    String resultText = result.trim().toUpperCase();
                                    System.out.println("LLM Response: " + resultText);
                                    return resultText.contains("YES");
                                }
                            }
                        }
                    }
                } else {
                    System.err.println("Together.AI error: " + response.getStatusCode());
                }

                break; // exit loop on non-429 failure
            } catch (HttpClientErrorException.TooManyRequests e) {
                attempt++;
                System.err.println("Rate limited by Together.AI - retry " + attempt + "/" + MAX_RETRIES);
                try {
                    Thread.sleep(RETRY_DELAY_MS * attempt); // Exponential backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            } catch (Exception e) {
                System.err.println("Exception during Together.AI call:");
                e.printStackTrace();
                break; // For other exceptions, exit immediately
            }
        }

        return false; // Default fallback
    }
}
