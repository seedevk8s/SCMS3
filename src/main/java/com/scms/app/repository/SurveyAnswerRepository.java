package com.scms.app.repository;

import com.scms.app.model.SurveyAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurveyAnswerRepository extends JpaRepository<SurveyAnswer, Long> {

    /**
     * 응답별 답변 목록 조회
     */
    @Query("SELECT a FROM SurveyAnswer a WHERE a.responseId = :responseId ORDER BY a.questionId")
    List<SurveyAnswer> findByResponseIdOrderByQuestionId(@Param("responseId") Long responseId);

    /**
     * 질문별 답변 목록 조회 (통계용)
     */
    @Query("SELECT a FROM SurveyAnswer a WHERE a.questionId = :questionId")
    List<SurveyAnswer> findByQuestionId(@Param("questionId") Long questionId);

    /**
     * 특정 선택지를 선택한 응답 수 조회
     */
    @Query("SELECT COUNT(a) FROM SurveyAnswer a WHERE a.questionId = :questionId AND a.optionId = :optionId")
    long countByQuestionIdAndOptionId(@Param("questionId") Long questionId,
                                       @Param("optionId") Long optionId);

    /**
     * 질문별 척도 평균 계산
     */
    @Query("SELECT AVG(a.answerNumber) FROM SurveyAnswer a WHERE a.questionId = :questionId AND a.answerNumber IS NOT NULL")
    Double calculateAverageScale(@Param("questionId") Long questionId);

    /**
     * 응답별 답변 삭제
     */
    void deleteByResponseId(Long responseId);

    /**
     * 응답 ID 목록에 해당하는 모든 답변 조회
     */
    @Query("SELECT a FROM SurveyAnswer a WHERE a.responseId IN :responseIds ORDER BY a.responseId, a.questionId")
    List<SurveyAnswer> findByResponseIdIn(@Param("responseIds") List<Long> responseIds);
}
