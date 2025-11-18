package com.scms.app.repository;

import com.scms.app.model.SurveyQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurveyQuestionRepository extends JpaRepository<SurveyQuestion, Long> {

    /**
     * 설문별 질문 목록 조회 (순서대로)
     */
    @Query("SELECT q FROM SurveyQuestion q WHERE q.surveyId = :surveyId ORDER BY q.displayOrder ASC")
    List<SurveyQuestion> findBySurveyIdOrderByDisplayOrder(@Param("surveyId") Long surveyId);

    /**
     * 설문의 질문 수 조회
     */
    @Query("SELECT COUNT(q) FROM SurveyQuestion q WHERE q.surveyId = :surveyId")
    long countBySurveyId(@Param("surveyId") Long surveyId);

    /**
     * 설문의 필수 질문 수 조회
     */
    @Query("SELECT COUNT(q) FROM SurveyQuestion q WHERE q.surveyId = :surveyId AND q.isRequired = true")
    long countRequiredBySurveyId(@Param("surveyId") Long surveyId);

    /**
     * 설문별 질문 삭제
     */
    void deleteBySurveyId(Long surveyId);
}
