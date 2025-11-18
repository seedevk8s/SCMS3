package com.scms.app.repository;

import com.scms.app.model.Survey;
import com.scms.app.model.SurveyTargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SurveyRepository extends JpaRepository<Survey, Long> {

    /**
     * 삭제되지 않은 설문 조회
     */
    @Query("SELECT s FROM Survey s WHERE s.surveyId = :surveyId AND s.deletedAt IS NULL")
    Optional<Survey> findByIdAndNotDeleted(@Param("surveyId") Long surveyId);

    /**
     * 활성화된 설문 목록 조회 (페이징)
     */
    @Query("SELECT s FROM Survey s WHERE s.isActive = true AND s.deletedAt IS NULL ORDER BY s.createdAt DESC")
    Page<Survey> findActivesurveys(Pageable pageable);

    /**
     * 현재 진행 중인 설문 목록 조회
     */
    @Query("SELECT s FROM Survey s WHERE s.isActive = true AND s.deletedAt IS NULL " +
           "AND :now >= s.startDate AND :now <= s.endDate ORDER BY s.startDate DESC")
    List<Survey> findOngoingSurveys(@Param("now") LocalDateTime now);

    /**
     * 생성자별 설문 조회
     */
    @Query("SELECT s FROM Survey s WHERE s.createdBy = :userId AND s.deletedAt IS NULL ORDER BY s.createdAt DESC")
    List<Survey> findByCreatedBy(@Param("userId") Integer userId);

    /**
     * 제목으로 검색 (페이징)
     */
    @Query("SELECT s FROM Survey s WHERE s.title LIKE %:keyword% AND s.deletedAt IS NULL ORDER BY s.createdAt DESC")
    Page<Survey> searchByTitle(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 대상 유형별 설문 조회
     */
    @Query("SELECT s FROM Survey s WHERE s.targetType = :targetType AND s.isActive = true AND s.deletedAt IS NULL ORDER BY s.createdAt DESC")
    List<Survey> findByTargetType(@Param("targetType") SurveyTargetType targetType);

    /**
     * 전체 설문 수 (삭제되지 않은 것만)
     */
    @Query("SELECT COUNT(s) FROM Survey s WHERE s.deletedAt IS NULL")
    long countNotDeleted();

    /**
     * 활성 설문 수
     */
    @Query("SELECT COUNT(s) FROM Survey s WHERE s.isActive = true AND s.deletedAt IS NULL")
    long countActive();
}
