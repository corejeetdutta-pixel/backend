package com.recruitment.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.dto.EvaluationRequest;
import com.recruitment.dto.EvaluationResponse;
import com.recruitment.dto.GenerateQuestionsRequest;
import com.recruitment.dto.GenerateQuestionsResponse;
import com.recruitment.service.EvaluationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/ai/jd")
public class AIController {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final EvaluationService evaluationService;
    private final String apiUrl;
    private final String apiKey;

    public AIController(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            EvaluationService evaluationService,
            @Value("${together.api.key}") String apiKey,
            @Value("${spring.ai.openai.base-url}") String apiUrl
    ) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.evaluationService = evaluationService;
        this.apiKey = apiKey;
        this.apiUrl = apiUrl.trim();
    }

    // ===== Helper Method to call Together API =====
    private String callTogetherAI(String userPrompt) {
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("model", "meta-llama/Llama-3-70b-chat-hf");
            request.put("max_tokens", 2048);
            request.put("temperature", 0.7);
            request.put("messages", List.of(Map.of("role", "user", "content", userPrompt)));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode choices = root.path("choices");
                if (choices.isArray() && choices.size() > 0) {
                    return choices.get(0).path("message").path("content").asText();
                }
            }
            return "Error: Failed to get valid response from AI";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error calling Together.ai: " + e.getMessage();
        }
    }

    // ===== JD Generator =====
    @PostMapping("/generate")
    public ResponseEntity<String> generateJD(@RequestBody Map<String, String> jobDetails) {
        System.out.println("JD generation triggered");

        String prompt = String.format("""
            You are a professional job description generator.
            Based on the following job information, generate a JSON object containing ONLY these fields:
            {
              "description": "...",
              "responsibilities": "...",
              "perks": "...",
              "requirements": "..."
            }

            Do NOT include any extra text or explanation.

            Job Details:
            Title: %s
            Company: %s
            Location: %s
            Department: %s
            Mode: %s
            Experience: %s
            Highest Qualification: %s
            Min Salary: %s
            Max Salary: %s
            Openings: %s
            Opening Date: %s
            Closing Date: %s
            Hiring Manager Email: %s
            """,
                jobDetails.getOrDefault("title", ""),
                jobDetails.getOrDefault("company", ""),
                jobDetails.getOrDefault("location", ""),
                jobDetails.getOrDefault("department", ""),
                jobDetails.getOrDefault("mode", ""),
                jobDetails.getOrDefault("experience", ""),
                jobDetails.getOrDefault("highestQualification", ""),
                jobDetails.getOrDefault("minSalary", ""),
                jobDetails.getOrDefault("maxSalary", ""),
                jobDetails.getOrDefault("openings", ""),
                jobDetails.getOrDefault("openingDate", ""),
                jobDetails.getOrDefault("closingDate", ""),
                jobDetails.getOrDefault("contactEmail", "")
        );

        String response = callTogetherAI(prompt);
        return ResponseEntity.ok(response);
    }

    // ===== Question Generator =====
    @PostMapping("/generate-questions")
    public GenerateQuestionsResponse generateQuestions(@RequestBody GenerateQuestionsRequest request) {
        System.out.println("Question generation triggered with experience level: " + request.getExperienceLevel());

        int questionCount = request.getCount() > 0 ? request.getCount() : 5;
        String difficultyLevel = determineDifficultyLevel(request.getExperienceLevel());

        String prompt = String.format("""
            You are a professional technical interviewer generating unique questions for %s position.

            JOB CONTEXT:
            - Title: %s
            - Description: %s
            - Requirements: %s
            - Experience Level: %s (%s)

            CRITICAL INSTRUCTIONS:
            1. Generate EXACTLY %d UNIQUE multiple-choice questions
            2. Difficulty Level: %s
            3. Each question must have:
               - question
               - options: exactly 4
               - correctOption (0-3)
            4. Format: JSON array only
            """,
                request.getTitle(),
                request.getTitle(),
                request.getDescription() != null ? request.getDescription() : "",
                request.getRequirements() != null ? request.getRequirements() : "",
                request.getExperienceLevel() != null ? request.getExperienceLevel() : "Not specified",
                difficultyLevel,
                questionCount,
                difficultyLevel
        );

        String rawResponse = callTogetherAI(prompt);
        String jsonResponse = extractJsonFromResponse(rawResponse);

        try {
            List<GenerateQuestionsResponse.MultipleChoiceQuestion> questions =
                    objectMapper.readValue(
                            jsonResponse,
                            objectMapper.getTypeFactory().constructCollectionType(List.class,
                                    GenerateQuestionsResponse.MultipleChoiceQuestion.class)
                    );

            if (questions.size() > questionCount) {
                questions = questions.subList(0, questionCount);
            }

            return new GenerateQuestionsResponse(questions);
        } catch (Exception e) {
            e.printStackTrace();
            return generateFallbackQuestions(request.getTitle(), difficultyLevel);
        }
    }

    private String determineDifficultyLevel(String experience) {
        if (experience == null || experience.trim().isEmpty()) {
            return "intermediate";
        }
        try {
            String cleanExp = experience.replaceAll("[^0-9\\-+]", "").trim();
            if (cleanExp.contains("+")) {
                int minExp = Integer.parseInt(cleanExp.replace("+", ""));
                return getDifficultyForExperience(minExp);
            } else if (cleanExp.contains("-")) {
                int minExp = Integer.parseInt(cleanExp.split("-")[0].trim());
                return getDifficultyForExperience(minExp);
            } else {
                int exp = Integer.parseInt(cleanExp);
                return getDifficultyForExperience(exp);
            }
        } catch (Exception e) {
            return "intermediate";
        }
    }

    private String getDifficultyForExperience(int years) {
        if (years <= 2) return "basic";
        else if (years <= 4) return "intermediate";
        else if (years <= 6) return "moderately complex";
        else if (years <= 10) return "complex";
        else if (years <= 15) return "highly complex";
        else if (years <= 20) return "expert-level";
        else return "expert-plus-level";
    }

    private String extractJsonFromResponse(String rawResponse) {
        int startIndex = rawResponse.indexOf('[');
        int endIndex = rawResponse.lastIndexOf(']');
        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            return rawResponse.substring(startIndex, endIndex + 1);
        }
        return rawResponse;
    }

    private GenerateQuestionsResponse generateFallbackQuestions(String title, String difficulty) {
        List<GenerateQuestionsResponse.MultipleChoiceQuestion> questions = new ArrayList<>();
        return new GenerateQuestionsResponse(questions);
    }

    // ===== Answer Evaluation =====
    @PostMapping("/evaluate")
    public EvaluationResponse evaluate(@RequestBody EvaluationRequest request) {
        System.out.println("evaluation triggered");

        List<String> questions = request.getQuestions();
        List<String> answers = request.getAnswers();

        int totalScore = 0;
        for (int i = 0; i < questions.size(); i++) {
            String q = questions.get(i);
            String a = answers.size() > i ? answers.get(i) : "";
            boolean relevant = evaluationService.isAnswerRelevant(q, a);
            if (relevant) totalScore += 10;
        }
        boolean qualified = totalScore >= 70;
        return new EvaluationResponse(totalScore, qualified);
    }

    // ===== Resume Enhancement =====
    @PostMapping("/cv/enhance")
    public ResponseEntity<?> enhanceResume(@RequestBody Map<String, String> request) {
        System.out.println("resume enhancement triggered");

        String resumeText = request.getOrDefault("resume", "");
        String jobTitle = request.getOrDefault("jobTitle", "");
        String company = request.getOrDefault("company", "");
        String jobDescription = request.getOrDefault("jobDescription", "");

        resumeText = trimToLength(resumeText, 3000);
        jobDescription = trimToLength(jobDescription, 2000);

        String prompt = String.format("""
            You are a professional CV writer.
            Rewrite this resume to match the job description.

            Resume:
            %s

            Job Title: %s
            Company: %s
            Job Description: %s

            Return ONLY the enhanced resume.
        """, resumeText, jobTitle, company, jobDescription);

        String enhancedCv = callTogetherAI(prompt);
        return ResponseEntity.ok(Map.of("enhancedCv", enhancedCv));
    }

    private String trimToLength(String text, int maxChars) {
        return text != null && text.length() > maxChars ? text.substring(0, maxChars) : text;
    }
}
