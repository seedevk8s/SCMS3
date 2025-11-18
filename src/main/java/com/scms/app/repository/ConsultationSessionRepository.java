package com.scms.app.repository;

import com.scms.app.model.ConsultationSession;
import com.scms.app.model.ConsultationStatus;
import com.scms.app.model.ConsultationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 상담 세션 Repository
 */
@Repository
public interface ConsultationSessionRepository extends JpaRepository<ConsultationSession, Long> {

    /**
     * 세션 ID로 조회 (삭제되지 않은 것만, 관계 엔티티 JOIN FETCH)
     */
    @Query("SELECT cs FROM ConsultationSession cs " +
           "LEFT JOIN FETCH cs.student " +
           "LEFT JOIN FETCH cs.counselor " +
           "WHERE cs.sessionId = :sessionId " +
           "AND cs.deletedAt IS NULL")
    Optional<ConsultationSession> findByIdAndNotDeleted(@Param("sessionId") Long sessionId);

    /**
     * 학생의 모든 상담 세션 조회 (삭제되지 않은 것만, 최신순)
     */
    @Query("SELECT cs FROM ConsultationSession cs " +
           "LEFT JOIN FETCH cs.student " +
           "LEFT JOIN FETCH cs.counselor " +
           "WHERE cs.student.userId = :studentId " +
           "AND cs.deletedAt IS NULL " +
           "ORDER BY cs.createdAt DESC")
    List<ConsultationSession> findByStudentId(@Param("studentId") Integer studentId);

    /**
     * 상담사의 모든 상담 세션 조회 (삭제되지 않은 것만, 최신순)
     * - 자신에게 배정된 상담 + 미배정 상담(PENDING 상태)
     */
    @Query("SELECT cs FROM ConsultationSession cs " +
           "LEFT JOIN FETCH cs.student " +
           "LEFT JOIN FETCH cs.counselor " +
           "WHERE (cs.counselor.counselorId = :counselorId OR (cs.counselor IS NULL AND cs.status = 'PENDING')) " +
           "AND cs.deletedAt IS NULL " +
           "ORDER BY cs.createdAt DESC")
    List<ConsultationSession> findByCounselorId(@Param("counselorId") Integer counselorId);

    /**
     * 학생의 특정 상태 상담 세션 조회
     */
    @Query("SELECT cs FROM ConsultationSession cs " +
           "LEFT JOIN FETCH cs.student " +
           "LEFT JOIN FETCH cs.counselor " +
           "WHERE cs.student.userId = :studentId " +
           "AND cs.status = :status " +
           "AND cs.deletedAt IS NULL " +
           "ORDER BY cs.createdAt DESC")
    List<ConsultationSession> findByStudentIdAndStatus(
            @Param("studentId") Integer studentId,
            @Param("status") ConsultationStatus status);

    /**
     * 상담사의 특정 상태 상담 세션 조회
     * - 자신에게 배정된 상담 + 미배정 PENDING 상담
     */
    @Query("SELECT cs FROM ConsultationSession cs " +
           "LEFT JOIN FETCH cs.student " +
           "LEFT JOIN FETCH cs.counselor " +
           "WHERE (cs.counselor.counselorId = :counselorId OR (cs.counselor IS NULL AND cs.status = 'PENDING')) " +
           "AND cs.status = :status " +
           "AND cs.deletedAt IS NULL " +
           "ORDER BY cs.createdAt DESC")
    List<ConsultationSession> findByCounselorIdAndStatus(
            @Param("counselorId") Integer counselorId,
            @Param("status") ConsultationStatus status);

    /**
     * 학생의 특정 유형 상담 세션 조회
     */
    @Query("SELECT cs FROM ConsultationSession cs " +
           "LEFT JOIN FETCH cs.student " +
           "LEFT JOIN FETCH cs.counselor " +
           "WHERE cs.student.userId = :studentId " +
           "AND cs.consultationType = :type " +
           "AND cs.deletedAt IS NULL " +
           "ORDER BY cs.createdAt DESC")
    List<ConsultationSession> findByStudentIdAndType(
            @Param("studentId") Integer studentId,
            @Param("type") ConsultationType type);

    /**
     * 특정 상태의 모든 상담 세션 조회 (관리자용)
     */
    @Query("SELECT cs FROM ConsultationSession cs " +
           "LEFT JOIN FETCH cs.student " +
           "LEFT JOIN FETCH cs.counselor " +
           "WHERE cs.status = :status " +
           "AND cs.deletedAt IS NULL " +
           "ORDER BY cs.createdAt DESC")
    List<ConsultationSession> findByStatus(@Param("status") ConsultationStatus status);

    /**
     * 모든 상담 세션 조회 (삭제되지 않은 것만, 관리자용)
     */
    @Query("SELECT cs FROM ConsultationSession cs " +
           "LEFT JOIN FETCH cs.student " +
           "LEFT JOIN FETCH cs.counselor " +
           "WHERE cs.deletedAt IS NULL " +
           "ORDER BY cs.createdAt DESC")
    List<ConsultationSession> findAllNotDeleted();

    /**
     * 학생의 활성 상담 세션 개수 조회 (대기/승인 상태)
     */
    @Query("SELECT COUNT(cs) FROM ConsultationSession cs " +
           "WHERE cs.student.userId = :studentId " +
           "AND cs.status IN ('PENDING', 'APPROVED') " +
           "AND cs.deletedAt IS NULL")
    Long countActiveSessionsByStudentId(@Param("studentId") Integer studentId);

    /**
     * 상담사의 특정 날짜의 승인된 세션 조회
     */
    @Query("SELECT cs FROM ConsultationSession cs " +
           "LEFT JOIN FETCH cs.student " +
           "LEFT JOIN FETCH cs.counselor " +
           "WHERE cs.counselor.counselorId = :counselorId " +
           "AND cs.requestedDate = :date " +
           "AND cs.status = 'APPROVED' " +
           "AND cs.deletedAt IS NULL " +
           "ORDER BY cs.requestedTime")
    List<ConsultationSession> findByCounselorIdAndDate(
            @Param("counselorId") Integer counselorId,
            @Param("date") LocalDate date);

    /**
     * 특정 기간의 상담 세션 조회 (통계용)
     */
    @Query("SELECT cs FROM ConsultationSession cs " +
           "WHERE cs.requestedDate BETWEEN :startDate AND :endDate " +
           "AND cs.deletedAt IS NULL " +
           "ORDER BY cs.requestedDate")
    List<ConsultationSession> findByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
