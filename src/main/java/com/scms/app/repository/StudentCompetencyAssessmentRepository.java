package com.scms.app.repository;

import com.scms.app.model.StudentCompetencyAssessment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 학생 역량 평가 Repository
 */
@Repository
public interface StudentCompetencyAssessmentRepository extends JpaRepository<StudentCompetencyAssessment, Long> {

    /**
     * 학생별 평가 조회
     */
    List<StudentCompetencyAssessment> findByStudentId(Long studentId);

    /**
     * 학생별 평가 조회 (평가일 순)
     */
    List<StudentCompetencyAssessment> findByStudentIdOrderByAssessmentDateDesc(Long studentId);

    /**
     * 역량별 평가 조회
     */
    List<StudentCompetencyAssessment> findByCompetencyId(Long competencyId);

    /**
     * 학생-역량 평가 조회 (최신순 첫 번째)
     */
    Optional<StudentCompetencyAssessment> findFirstByStudentIdAndCompetencyIdOrderByAssessmentDateDesc(
        Long studentId, Long competencyId
    );

    /**
     * 학생-역량 전체 평가 이력
     */
    List<StudentCompetencyAssessment> findByStudentIdAndCompetencyIdOrderByAssessmentDateDesc(
        Long studentId, Long competencyId
    );

    /**
     * 평가 날짜 범위 조회
     */
    List<StudentCompetencyAssessment> findByAssessmentDateBetween(
        LocalDate startDate, LocalDate endDate
    );

    /**
     * 학생-평가 날짜 범위 조회
     */
    List<StudentCompetencyAssessment> findByStudentIdAndAssessmentDateBetween(
        Long studentId, LocalDate startDate, LocalDate endDate
    );

    /**
     * 페이지네이션: 학생별 평가
     */
    Page<StudentCompetencyAssessment> findByStudentId(
        Long studentId, Pageable pageable
    );

    /**
     * 학생별 평가 조회 (역량, 카테고리 정보 포함)
     */
    @Query("SELECT a FROM StudentCompetencyAssessment a " +
           "JOIN FETCH a.competency c " +
           "JOIN FETCH c.category " +
           "WHERE a.student.id = :studentId " +
           "ORDER BY a.assessmentDate DESC")
    List<StudentCompetencyAssessment> findByStudentIdWithDetails(@Param("studentId") Long studentId);

    /**
     * 학생별 최신 평가만 조회 (각 역량당 최신 1건)
     */
    @Query("SELECT a FROM StudentCompetencyAssessment a " +
           "JOIN FETCH a.competency c " +
           "JOIN FETCH c.category " +
           "WHERE a.student.id = :studentId " +
           "AND a.assessmentDate = (" +
           "  SELECT MAX(a2.assessmentDate) " +
           "  FROM StudentCompetencyAssessment a2 " +
           "  WHERE a2.student.id = a.student.id " +
           "  AND a2.competency.id = a.competency.id" +
           ") " +
           "ORDER BY c.category.id, c.id")
    List<StudentCompetencyAssessment> findLatestAssessmentsByStudentId(@Param("studentId") Long studentId);

    /**
     * 역량별 평균 점수 조회
     */
    @Query("SELECT AVG(a.score) FROM StudentCompetencyAssessment a WHERE a.competency.id = :competencyId")
    Double findAverageScoreByCompetencyId(@Param("competencyId") Long competencyId);

    /**
     * 학생의 전체 평균 점수
     */
    @Query("SELECT AVG(a.score) FROM StudentCompetencyAssessment a WHERE a.student.id = :studentId")
    Double findAverageScoreByStudentId(@Param("studentId") Long studentId);

    /**
     * 카테고리별 평균 점수 (학생별)
     */
    @Query("SELECT AVG(a.score) FROM StudentCompetencyAssessment a " +
           "WHERE a.student.id = :studentId " +
           "AND a.competency.category.id = :categoryId")
    Double findAverageScoreByStudentIdAndCategoryId(
        @Param("studentId") Long studentId,
        @Param("categoryId") Long categoryId
    );

    /**
     * 학생의 평가 개수
     */
    long countByStudentId(Long studentId);

    /**
     * 특정 점수 이상인 평가 조회
     */
    @Query("SELECT a FROM StudentCompetencyAssessment a " +
           "WHERE a.student.id = :studentId " +
           "AND a.score >= :minScore " +
           "ORDER BY a.score DESC")
    List<StudentCompetencyAssessment> findByStudentIdAndScoreGreaterThanEqual(
        @Param("studentId") Long studentId,
        @Param("minScore") Integer minScore
    );

    /**
     * 특정 점수 미만인 평가 조회 (개선 필요 역량)
     */
    @Query("SELECT a FROM StudentCompetencyAssessment a " +
           "WHERE a.student.id = :studentId " +
           "AND a.score < :maxScore " +
           "ORDER BY a.score ASC")
    List<StudentCompetencyAssessment> findByStudentIdAndScoreLessThan(
        @Param("studentId") Long studentId,
        @Param("maxScore") Integer maxScore
    );
}
