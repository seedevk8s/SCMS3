package com.scms.app.repository;

import com.scms.app.model.SurveyResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SurveyResponseRepository extends JpaRepository<SurveyResponse, Long> {

    /**
     * 설문별 응답 목록 조회
     */
    @Query("SELECT r FROM SurveyResponse r WHERE r.surveyId = :surveyId ORDER BY r.submittedAt DESC")
    List<SurveyResponse> findBySurveyIdOrderBySubmittedAtDesc(@Param("surveyId") Long surveyId);

    /**
     * 사용자별 응답 목록 조회
     */
    @Query("SELECT r FROM SurveyResponse r WHERE r.userId = :userId ORDER BY r.submittedAt DESC")
    List<SurveyResponse> findByUserIdOrderBySubmittedAtDesc(@Param("userId") Integer userId);

    /**
     * 특정 사용자가 특정 설문에 응답했는지 확인
     */
    @Query("SELECT r FROM SurveyResponse r WHERE r.surveyId = :surveyId AND r.userId = :userId")
    Optional<SurveyResponse> findBySurveyIdAndUserId(@Param("surveyId") Long surveyId,
                                                       @Param("userId") Integer userId);

    /**
     * 사용자가 설문에 이미 응답했는지 확인
     */
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM SurveyResponse r " +
           "WHERE r.surveyId = :surveyId AND r.userId = :userId")
    boolean existsBySurveyIdAndUserId(@Param("surveyId") Long surveyId,
                                       @Param("userId") Integer userId);

    /**
     * 설문별 응답 수 조회
     */
    @Query("SELECT COUNT(r) FROM SurveyResponse r WHERE r.surveyId = :surveyId")
    long countBySurveyId(@Param("surveyId") Long surveyId);

    /**
     * 설문별 응답 삭제
     */
    void deleteBySurveyId(Long surveyId);
}
