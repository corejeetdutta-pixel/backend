package com.recruitment.service;

import com.recruitment.entity.Question;
import com.recruitment.repository.QuestionRepository;
import com.recruitment.dto.GenerateQuestionsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuestionService {
    @Autowired
    private QuestionRepository questionRepository;
    
    // Add this method to generate questions (pseudo-implementation)
    private List<GenerateQuestionsResponse.MultipleChoiceQuestion> 
        generateQuestionsWithAI(String jobTitle, String difficulty, int count) {
        
        // Implement your actual AI question generation logic here
        // This is a placeholder implementation
        List<GenerateQuestionsResponse.MultipleChoiceQuestion> questions = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            GenerateQuestionsResponse.MultipleChoiceQuestion question = 
                new GenerateQuestionsResponse.MultipleChoiceQuestion();
            question.setQuestion("Sample question about " + jobTitle + " (" + difficulty + ")");
            // Set other properties like options, correct answer, etc.
            questions.add(question);
        }
        
        return questions;
    }

    public List<GenerateQuestionsResponse.MultipleChoiceQuestion> 
        getUniqueQuestions(String jobTitle, String difficulty, int count) {
        
        Set<String> usedQuestions = questionRepository.findUsedQuestionsForJob(jobTitle);
        
        List<GenerateQuestionsResponse.MultipleChoiceQuestion> newQuestions = 
            generateQuestionsWithAI(jobTitle, difficulty, count + 10);
        
        List<GenerateQuestionsResponse.MultipleChoiceQuestion> uniqueQuestions = new ArrayList<>();
        for (GenerateQuestionsResponse.MultipleChoiceQuestion question : newQuestions) {
            if (!usedQuestions.contains(question.getQuestion())) {
                uniqueQuestions.add(question);
                if (uniqueQuestions.size() >= count) break;
            }
        }
        
        // Save new questions to database
        List<Question> questionsToSave = uniqueQuestions.stream()
            .map(q -> new Question(q.getQuestion(), jobTitle, difficulty))
            .collect(Collectors.toList());
        
        questionRepository.saveAll(questionsToSave);
        
        return uniqueQuestions.subList(0, Math.min(count, uniqueQuestions.size()));
    }
}