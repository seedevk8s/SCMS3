package com.scms.app.repository;

import com.scms.app.model.CounselorSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 상담사 일정 Repository
 */
@Repository
public interface CounselorScheduleRepository extends JpaRepository<CounselorSchedule, Long> {

    /**
     * 일정 ID로 조회 (삭제되지 않은 것만)
     */
    @Query("SELECT cs FROM CounselorSchedule cs " +
           "LEFT JOIN FETCH cs.counselor " +
           "WHERE cs.scheduleId = :scheduleId " +
           "AND cs.deletedAt IS NULL")
    Optional<CounselorSchedule> findByIdAndNotDeleted(@Param("scheduleId") Long scheduleId);

    /**
     * 상담사의 모든 일정 조회 (삭제되지 않은 것만)
     */
    @Query("SELECT cs FROM CounselorSchedule cs " +
           "LEFT JOIN FETCH cs.counselor " +
           "WHERE cs.counselor.counselorId = :counselorId " +
           "AND cs.deletedAt IS NULL " +
           "ORDER BY cs.dayOfWeek, cs.startTime")
    List<CounselorSchedule> findByCounselorId(@Param("counselorId") Integer counselorId);

    /**
     * 상담사의 특정 요일 일정 조회
     */
    @Query("SELECT cs FROM CounselorSchedule cs " +
           "LEFT JOIN FETCH cs.counselor " +
           "WHERE cs.counselor.counselorId = :counselorId " +
           "AND cs.dayOfWeek = :dayOfWeek " +
           "AND cs.deletedAt IS NULL " +
           "ORDER BY cs.startTime")
    List<CounselorSchedule> findByCounselorIdAndDayOfWeek(
            @Param("counselorId") Integer counselorId,
            @Param("dayOfWeek") Integer dayOfWeek);

    /**
     * 상담사의 가용한 일정만 조회
     */
    @Query("SELECT cs FROM CounselorSchedule cs " +
           "LEFT JOIN FETCH cs.counselor " +
           "WHERE cs.counselor.counselorId = :counselorId " +
           "AND cs.isAvailable = true " +
           "AND cs.deletedAt IS NULL " +
           "ORDER BY cs.dayOfWeek, cs.startTime")
    List<CounselorSchedule> findAvailableSchedulesByCounselorId(@Param("counselorId") Integer counselorId);

    /**
     * 상담사의 특정 요일 가용 일정 조회
     */
    @Query("SELECT cs FROM CounselorSchedule cs " +
           "LEFT JOIN FETCH cs.counselor " +
           "WHERE cs.counselor.counselorId = :counselorId " +
           "AND cs.dayOfWeek = :dayOfWeek " +
           "AND cs.isAvailable = true " +
           "AND cs.deletedAt IS NULL " +
           "ORDER BY cs.startTime")
    List<CounselorSchedule> findAvailableSchedulesByCounselorIdAndDayOfWeek(
            @Param("counselorId") Integer counselorId,
            @Param("dayOfWeek") Integer dayOfWeek);

    /**
     * 특정 요일에 근무하는 모든 상담사의 일정 조회
     */
    @Query("SELECT cs FROM CounselorSchedule cs " +
           "LEFT JOIN FETCH cs.counselor " +
           "WHERE cs.dayOfWeek = :dayOfWeek " +
           "AND cs.isAvailable = true " +
           "AND cs.deletedAt IS NULL " +
           "ORDER BY cs.counselor.counselorId, cs.startTime")
    List<CounselorSchedule> findByDayOfWeekAndAvailable(@Param("dayOfWeek") Integer dayOfWeek);

    /**
     * 상담사의 일정 존재 여부 확인
     */
    @Query("SELECT COUNT(cs) > 0 FROM CounselorSchedule cs " +
           "WHERE cs.counselor.counselorId = :counselorId " +
           "AND cs.deletedAt IS NULL")
    boolean existsByCounselorId(@Param("counselorId") Integer counselorId);
}
