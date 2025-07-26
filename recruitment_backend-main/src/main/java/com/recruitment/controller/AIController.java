package com.recruitment.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.dto.EvaluationRequest;
import com.recruitment.dto.EvaluationResponse;
import com.recruitment.dto.GenerateQuestionsRequest;
import com.recruitment.dto.GenerateQuestionsResponse;
import com.recruitment.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/ai/jd")
@CrossOrigin(origins = {"http://localhost:5173", "https://1c.atract.in"}, allowCredentials = "true")
public class AIController {
        @Autowired
        private final RestTemplate restTemplate;
        @Autowired
        private final ObjectMapper objectMapper;
        @Autowired
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
            this.apiUrl = apiUrl;
        }

        private String callTogetherAI(String userPrompt) {
            try {
                Map<String, Object> request = new HashMap<>();
                request.put("model", "meta-llama/Llama-3-70b-chat-hf");
                request.put("max_tokens", 1024);
                request.put("messages", List.of(Map.of("role", "user", "content", userPrompt)));

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBearerAuth(apiKey);

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
                ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);

                JsonNode root = objectMapper.readTree(response.getBody());
                return root.path("choices").get(0).path("message").path("content").asText();
            } catch (Exception e) {
                e.printStackTrace();
                return "Error calling Together.ai";
            }
        }

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

    @PostMapping("/generate-questions")
    public GenerateQuestionsResponse generateQuestions(@RequestBody GenerateQuestionsRequest request) {
        System.out.println("question generation triggered");

        String prompt = String.format("""
            You are a professional interviewer.
            For the job title "%s", generate exactly 5 multiple-choice questions.
            Each question must have:
            - question: the question text
            - options: an array of 4 options (short, clear)
            - correctOption: the index (0-3) of the correct option

            Return a JSON array like:
            [
              {"question":"...","options":["...","...","...","..."],"correctOption":1},
              {"question":"...","options":["...","...","...","..."],"correctOption":0},
              ...
            ]
            Only return the JSON array, no explanations.
        """, request.getTitle());

        String rawResponse = callTogetherAI(prompt);

        try {
            List<GenerateQuestionsResponse.MultipleChoiceQuestion> questions =
                objectMapper.readValue(
                    rawResponse,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, GenerateQuestionsResponse.MultipleChoiceQuestion.class)
                );
            return new GenerateQuestionsResponse(questions);
        } catch (Exception e) {
            e.printStackTrace();
            return new GenerateQuestionsResponse(List.of());
        }
    }

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

    @PostMapping("/cv/enhance")
    public ResponseEntity<?> enhanceResume(@RequestBody Map<String, String> request) {
        System.out.println("resume enhancement triggered");

        String resumeText = request.getOrDefault("resume", "");
        String jobTitle = request.getOrDefault("jobTitle", "");
        String company = request.getOrDefault("company", "");
        String jobDescription = request.getOrDefault("jobDescription", "");

        // Limit lengths to avoid token overflow (you may tweak these numbers as needed)
        resumeText = trimToLength(resumeText, 3000); // ~3000 characters
        jobDescription = trimToLength(jobDescription, 2000); // ~2000 characters

        String prompt = String.format("""
            You are a professional CV writer.
            Given this resume and the job details, rewrite the resume to better match the job description.
            Make it clear, professional, and relevant.

            Resume:
            %s

            Job Title: %s
            Company: %s
            Job Description: %s

            Return ONLY the enhanced resume text, no extra explanation.
        """, resumeText, jobTitle, company, jobDescription);

        String enhancedCv = callTogetherAI(prompt);
        return ResponseEntity.ok(Map.of("enhancedCv", enhancedCv));
    }

    private String trimToLength(String text, int maxChars) {
        return text != null && text.length() > maxChars ? text.substring(0, maxChars) : text;
    }

}
