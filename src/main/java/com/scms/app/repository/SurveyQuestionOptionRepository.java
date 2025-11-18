package com.scms.app.repository;

import com.scms.app.model.SurveyQuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurveyQuestionOptionRepository extends JpaRepository<SurveyQuestionOption, Long> {

    /**
     * 질문별 선택지 목록 조회 (순서대로)
     */
    @Query("SELECT o FROM SurveyQuestionOption o WHERE o.questionId = :questionId ORDER BY o.displayOrder ASC")
    List<SurveyQuestionOption> findByQuestionIdOrderByDisplayOrder(@Param("questionId") Long questionId);

    /**
     * 질문별 선택지 삭제
     */
    void deleteByQuestionId(Long questionId);

    /**
     * 질문 ID 목록에 해당하는 모든 선택지 조회
     */
    @Query("SELECT o FROM SurveyQuestionOption o WHERE o.questionId IN :questionIds ORDER BY o.questionId, o.displayOrder")
    List<SurveyQuestionOption> findByQuestionIdIn(@Param("questionIds") List<Long> questionIds);
}
