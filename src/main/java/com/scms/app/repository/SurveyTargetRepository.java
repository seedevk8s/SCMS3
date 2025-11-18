package com.scms.app.repository;

import com.scms.app.model.SurveyTarget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SurveyTargetRepository extends JpaRepository<SurveyTarget, Long> {

    /**
     * 설문별 대상자 목록 조회
     */
    @Query("SELECT t FROM SurveyTarget t WHERE t.surveyId = :surveyId")
    List<SurveyTarget> findBySurveyId(@Param("surveyId") Long surveyId);

    /**
     * 사용자별 설문 대상자 조회
     */
    @Query("SELECT t FROM SurveyTarget t WHERE t.userId = :userId")
    List<SurveyTarget> findByUserId(@Param("userId") Integer userId);

    /**
     * 사용자가 특정 설문의 대상자인지 확인
     */
    @Query("SELECT t FROM SurveyTarget t WHERE t.surveyId = :surveyId AND t.userId = :userId")
    Optional<SurveyTarget> findBySurveyIdAndUserId(@Param("surveyId") Long surveyId,
                                                     @Param("userId") Integer userId);

    /**
     * 사용자가 설문 대상자인지 확인
     */
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM SurveyTarget t " +
           "WHERE t.surveyId = :surveyId AND t.userId = :userId")
    boolean existsBySurveyIdAndUserId(@Param("surveyId") Long surveyId,
                                       @Param("userId") Integer userId);

    /**
     * 설문별 대상자 수 조회
     */
    @Query("SELECT COUNT(t) FROM SurveyTarget t WHERE t.surveyId = :surveyId")
    long countBySurveyId(@Param("surveyId") Long surveyId);

    /**
     * 설문별 응답 완료자 수 조회
     */
    @Query("SELECT COUNT(t) FROM SurveyTarget t WHERE t.surveyId = :surveyId AND t.hasResponded = true")
    long countRespondedBySurveyId(@Param("surveyId") Long surveyId);

    /**
     * 응답 상태 업데이트
     */
    @Modifying
    @Query("UPDATE SurveyTarget t SET t.hasResponded = true WHERE t.surveyId = :surveyId AND t.userId = :userId")
    void markAsResponded(@Param("surveyId") Long surveyId, @Param("userId") Integer userId);

    /**
     * 설문별 대상자 삭제
     */
    void deleteBySurveyId(Long surveyId);
}
