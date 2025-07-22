package com.recruitment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.dto.EvaluationRequest;
import com.recruitment.dto.EvaluationResponse;
import com.recruitment.dto.GenerateQuestionsRequest;
import com.recruitment.dto.GenerateQuestionsResponse;
import com.recruitment.service.EvaluationService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai/jd")
@CrossOrigin(origins = "https://1c.atract.in/", allowCredentials = "true")
public class AIController {

    private final OllamaChatModel model;

    @Autowired
    private EvaluationService evaluationService;

    public AIController(OllamaChatModel model) {
        this.model = model;
    }

    @PostMapping("/generate")
    public ResponseEntity<String> generateJD(@RequestBody Map<String, String> jobDetails) {

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

        String response = model.call(new UserMessage(prompt));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/generate-questions")
    public GenerateQuestionsResponse generateQuestions(@RequestBody GenerateQuestionsRequest request) {
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

        String rawResponse = model.call(new UserMessage(prompt)).trim();

        try {
            ObjectMapper mapper = new ObjectMapper();
            List<GenerateQuestionsResponse.MultipleChoiceQuestion> questions =
                mapper.readValue(
                    rawResponse,
                    mapper.getTypeFactory().constructCollectionType(List.class, GenerateQuestionsResponse.MultipleChoiceQuestion.class)
                );
            return new GenerateQuestionsResponse(questions);
        } catch (Exception e) {
            e.printStackTrace();
            return new GenerateQuestionsResponse(List.of());
        }
    }

    @PostMapping("/evaluate")
    public EvaluationResponse evaluate(@RequestBody EvaluationRequest request) {
        System.out.println("evaluate button is triggered");
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
    	System.out.println("resume enhance is triggered");
        String resumeText = request.get("resume");
        String jobTitle = request.get("jobTitle");
        String company = request.get("company");
        String jobDescription = request.get("jobDescription");

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

        String enhancedCv = model.call(new UserMessage(prompt)).trim();
        System.out.println("cv is created");

        return ResponseEntity.ok(Map.of("enhancedCv", enhancedCv));
    }


}