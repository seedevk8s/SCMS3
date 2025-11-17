package com.scms.app.repository;

import com.scms.app.model.ApplicationStatus;
import com.scms.app.model.ProgramApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 프로그램 신청 Repository
 */
@Repository
public interface ProgramApplicationRepository extends JpaRepository<ProgramApplication, Integer> {

    /**
     * 사용자의 특정 프로그램 신청 조회 (삭제되지 않은 것만)
     */
    @Query("SELECT pa FROM ProgramApplication pa " +
           "JOIN FETCH pa.program " +
           "JOIN FETCH pa.user " +
           "WHERE pa.user.userId = :userId " +
           "AND pa.program.programId = :programId " +
           "AND pa.deletedAt IS NULL")
    Optional<ProgramApplication> findByUserIdAndProgramId(
            @Param("userId") Integer userId,
            @Param("programId") Integer programId);

    /**
     * 사용자의 모든 신청 내역 조회 (삭제되지 않은 것만, 최신순)
     */
    @Query("SELECT pa FROM ProgramApplication pa " +
           "JOIN FETCH pa.program " +
           "JOIN FETCH pa.user " +
           "WHERE pa.user.userId = :userId " +
           "AND pa.deletedAt IS NULL " +
           "ORDER BY pa.appliedAt DESC")
    List<ProgramApplication> findByUserId(@Param("userId") Integer userId);

    /**
     * 프로그램별 신청 내역 조회 (삭제되지 않은 것만)
     */
    @Query("SELECT pa FROM ProgramApplication pa " +
           "JOIN FETCH pa.program " +
           "JOIN FETCH pa.user " +
           "WHERE pa.program.programId = :programId " +
           "AND pa.deletedAt IS NULL " +
           "ORDER BY pa.appliedAt DESC")
    List<ProgramApplication> findByProgramId(@Param("programId") Integer programId);

    /**
     * 사용자의 특정 상태 신청 내역 조회
     */
    @Query("SELECT pa FROM ProgramApplication pa " +
           "JOIN FETCH pa.program " +
           "JOIN FETCH pa.user " +
           "WHERE pa.user.userId = :userId " +
           "AND pa.status = :status " +
           "AND pa.deletedAt IS NULL " +
           "ORDER BY pa.appliedAt DESC")
    List<ProgramApplication> findByUserIdAndStatus(
            @Param("userId") Integer userId,
            @Param("status") ApplicationStatus status);

    /**
     * 프로그램별 특정 상태 신청 내역 조회
     */
    @Query("SELECT pa FROM ProgramApplication pa " +
           "JOIN FETCH pa.program " +
           "JOIN FETCH pa.user " +
           "WHERE pa.program.programId = :programId " +
           "AND pa.status = :status " +
           "AND pa.deletedAt IS NULL " +
           "ORDER BY pa.appliedAt DESC")
    List<ProgramApplication> findByProgramIdAndStatus(
            @Param("programId") Integer programId,
            @Param("status") ApplicationStatus status);

    /**
     * 사용자의 특정 프로그램에 활성화된 신청이 있는지 확인
     * (취소/거부되지 않은 신청)
     */
    @Query("SELECT COUNT(pa) > 0 FROM ProgramApplication pa " +
           "WHERE pa.user.userId = :userId " +
           "AND pa.program.programId = :programId " +
           "AND pa.status IN ('PENDING', 'APPROVED', 'COMPLETED') " +
           "AND pa.deletedAt IS NULL")
    boolean existsActiveApplicationByUserAndProgram(
            @Param("userId") Integer userId,
            @Param("programId") Integer programId);

    /**
     * 프로그램의 승인된 신청 개수 조회
     */
    @Query("SELECT COUNT(pa) FROM ProgramApplication pa " +
           "WHERE pa.program.programId = :programId " +
           "AND pa.status = 'APPROVED' " +
           "AND pa.deletedAt IS NULL")
    Long countApprovedApplicationsByProgramId(@Param("programId") Integer programId);

    /**
     * 특정 상태의 모든 신청 조회 (User와 Program JOIN FETCH)
     */
    @Query("SELECT pa FROM ProgramApplication pa " +
           "JOIN FETCH pa.program " +
           "JOIN FETCH pa.user " +
           "WHERE pa.status = :status " +
           "AND pa.deletedAt IS NULL")
    List<ProgramApplication> findByStatus(@Param("status") ApplicationStatus status);
}
