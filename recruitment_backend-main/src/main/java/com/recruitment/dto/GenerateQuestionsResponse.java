package com.recruitment.dto;

import java.util.List;

public class GenerateQuestionsResponse {
    private List<MultipleChoiceQuestion> questions;

    public GenerateQuestionsResponse() {}

    public GenerateQuestionsResponse(List<MultipleChoiceQuestion> questions) {
        this.questions = questions;
    }

    public List<MultipleChoiceQuestion> getQuestions() {
        return questions;
    }

    public void setQuestions(List<MultipleChoiceQuestion> questions) {
        this.questions = questions;
    }

    public static class MultipleChoiceQuestion {
        private String question;
        private List<String> options;
        private int correctOption;

        public MultipleChoiceQuestion() {}

        public MultipleChoiceQuestion(String question, List<String> options, int correctOption) {
            this.question = question;
            this.options = options;
            this.correctOption = correctOption;
        }

        public String getQuestion() {
            return question;
        }

        public void setQuestion(String question) {
            this.question = question;
        }

        public List<String> getOptions() {
            return options;
        }

        public void setOptions(List<String> options) {
            this.options = options;
        }

        public int getCorrectOption() {
            return correctOption;
        }

        public void setCorrectOption(int correctOption) {
            this.correctOption = correctOption;
        }
    }
}
