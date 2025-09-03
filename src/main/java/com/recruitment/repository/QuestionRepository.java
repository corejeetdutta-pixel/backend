package com.recruitment.repository;

import com.recruitment.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    
    @Query("SELECT DISTINCT q.questionText FROM Question q WHERE q.jobTitle = :jobTitle")
    Set<String> findUsedQuestionsForJob(@Param("jobTitle") String jobTitle);
    
    // Custom query to find questions by job title and difficulty
    Set<Question> findByJobTitleAndDifficulty(String jobTitle, String difficulty);
}