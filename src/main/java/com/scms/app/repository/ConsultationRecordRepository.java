package com.scms.app.repository;

import com.scms.app.model.ConsultationRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 상담 기록 Repository
 */
@Repository
public interface ConsultationRecordRepository extends JpaRepository<ConsultationRecord, Long> {

    /**
     * 기록 ID로 조회 (삭제되지 않은 것만)
     */
    @Query("SELECT cr FROM ConsultationRecord cr " +
           "LEFT JOIN FETCH cr.session " +
           "LEFT JOIN FETCH cr.counselor " +
           "LEFT JOIN FETCH cr.student " +
           "WHERE cr.recordId = :recordId " +
           "AND cr.deletedAt IS NULL")
    Optional<ConsultationRecord> findByIdAndNotDeleted(@Param("recordId") Long recordId);

    /**
     * 세션 ID로 상담 기록 조회
     */
    @Query("SELECT cr FROM ConsultationRecord cr " +
           "LEFT JOIN FETCH cr.session " +
           "LEFT JOIN FETCH cr.counselor " +
           "LEFT JOIN FETCH cr.student " +
           "WHERE cr.session.sessionId = :sessionId " +
           "AND cr.deletedAt IS NULL")
    Optional<ConsultationRecord> findBySessionId(@Param("sessionId") Long sessionId);

    /**
     * 학생의 모든 상담 기록 조회
     */
    @Query("SELECT cr FROM ConsultationRecord cr " +
           "LEFT JOIN FETCH cr.session " +
           "LEFT JOIN FETCH cr.counselor " +
           "LEFT JOIN FETCH cr.student " +
           "WHERE cr.student.userId = :studentId " +
           "AND cr.deletedAt IS NULL " +
           "ORDER BY cr.consultationDate DESC")
    List<ConsultationRecord> findByStudentId(@Param("studentId") Integer studentId);

    /**
     * 상담사의 모든 상담 기록 조회
     */
    @Query("SELECT cr FROM ConsultationRecord cr " +
           "LEFT JOIN FETCH cr.session " +
           "LEFT JOIN FETCH cr.counselor " +
           "LEFT JOIN FETCH cr.student " +
           "WHERE cr.counselor.counselorId = :counselorId " +
           "AND cr.deletedAt IS NULL " +
           "ORDER BY cr.consultationDate DESC")
    List<ConsultationRecord> findByCounselorId(@Param("counselorId") Integer counselorId);

    /**
     * 모든 상담 기록 조회 (삭제되지 않은 것만)
     */
    @Query("SELECT cr FROM ConsultationRecord cr " +
           "LEFT JOIN FETCH cr.session " +
           "LEFT JOIN FETCH cr.counselor " +
           "LEFT JOIN FETCH cr.student " +
           "WHERE cr.deletedAt IS NULL " +
           "ORDER BY cr.consultationDate DESC")
    List<ConsultationRecord> findAllNotDeleted();

    /**
     * 추가 상담이 필요한 기록 조회
     */
    @Query("SELECT cr FROM ConsultationRecord cr " +
           "LEFT JOIN FETCH cr.session " +
           "LEFT JOIN FETCH cr.counselor " +
           "LEFT JOIN FETCH cr.student " +
           "WHERE cr.followUpRequired = true " +
           "AND cr.deletedAt IS NULL " +
           "ORDER BY cr.consultationDate DESC")
    List<ConsultationRecord> findByFollowUpRequired();

    /**
     * 특정 기간의 상담 기록 조회 (통계용)
     */
    @Query("SELECT cr FROM ConsultationRecord cr " +
           "WHERE cr.consultationDate BETWEEN :startDate AND :endDate " +
           "AND cr.deletedAt IS NULL " +
           "ORDER BY cr.consultationDate")
    List<ConsultationRecord> findByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 학생의 상담 기록 개수 조회
     */
    @Query("SELECT COUNT(cr) FROM ConsultationRecord cr " +
           "WHERE cr.student.userId = :studentId " +
           "AND cr.deletedAt IS NULL")
    Long countByStudentId(@Param("studentId") Integer studentId);

    /**
     * 상담사의 상담 기록 개수 조회
     */
    @Query("SELECT COUNT(cr) FROM ConsultationRecord cr " +
           "WHERE cr.counselor.counselorId = :counselorId " +
           "AND cr.deletedAt IS NULL")
    Long countByCounselorId(@Param("counselorId") Integer counselorId);
}
