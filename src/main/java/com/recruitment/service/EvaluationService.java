package com.recruitment.service;

import org.springframework.stereotype.Service;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class EvaluationService {
	

    @Autowired
    private OllamaChatModel model;

    /**
     * Uses Ollama to check if the answer is relevant to the question.
     * Returns true if relevant, false otherwise.
     */
    public boolean isAnswerRelevant(String question, String answer) {
        String prompt = String.format("""
        You are an AI that checks if an answer is relevant to a question.
        Question: "%s"
        Answer: "%s"
        Reply with only "YES" if relevant, or "NO" if irrelevant.
        """, question, answer);

        String result = model.call(new UserMessage(prompt)).trim().toUpperCase();
        return result.contains("YES");
    }

}
