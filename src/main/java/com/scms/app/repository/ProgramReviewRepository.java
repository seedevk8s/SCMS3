package com.scms.app.repository;

import com.scms.app.model.ProgramReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 프로그램 후기 Repository
 */
@Repository
public interface ProgramReviewRepository extends JpaRepository<ProgramReview, Integer> {

    /**
     * 프로그램별 후기 목록 조회 (삭제되지 않은 것만, 최신순)
     */
    @Query("SELECT r FROM ProgramReview r " +
           "JOIN FETCH r.user " +
           "JOIN FETCH r.program " +
           "WHERE r.program.programId = :programId " +
           "AND r.deletedAt IS NULL " +
           "ORDER BY r.createdAt DESC")
    List<ProgramReview> findByProgramIdAndDeletedAtIsNull(@Param("programId") Integer programId);

    /**
     * 사용자의 특정 프로그램 후기 조회 (중복 체크용)
     */
    @Query("SELECT r FROM ProgramReview r " +
           "JOIN FETCH r.user " +
           "JOIN FETCH r.program " +
           "WHERE r.user.userId = :userId " +
           "AND r.program.programId = :programId " +
           "AND r.deletedAt IS NULL")
    Optional<ProgramReview> findByUserIdAndProgramIdAndDeletedAtIsNull(
            @Param("userId") Integer userId,
            @Param("programId") Integer programId);

    /**
     * 특정 후기 조회 (수정/삭제용)
     */
    @Query("SELECT r FROM ProgramReview r " +
           "JOIN FETCH r.user " +
           "JOIN FETCH r.program " +
           "WHERE r.reviewId = :reviewId " +
           "AND r.deletedAt IS NULL")
    Optional<ProgramReview> findByIdAndDeletedAtIsNull(@Param("reviewId") Integer reviewId);

    /**
     * 사용자가 작성한 후기인지 확인
     */
    @Query("SELECT r FROM ProgramReview r " +
           "WHERE r.reviewId = :reviewId " +
           "AND r.user.userId = :userId " +
           "AND r.deletedAt IS NULL")
    Optional<ProgramReview> findByReviewIdAndUserId(
            @Param("reviewId") Integer reviewId,
            @Param("userId") Integer userId);

    /**
     * 프로그램의 평균 평점 계산
     */
    @Query("SELECT AVG(r.rating) FROM ProgramReview r " +
           "WHERE r.program.programId = :programId " +
           "AND r.deletedAt IS NULL")
    Double getAverageRatingByProgramId(@Param("programId") Integer programId);

    /**
     * 프로그램의 후기 개수 조회
     */
    @Query("SELECT COUNT(r) FROM ProgramReview r " +
           "WHERE r.program.programId = :programId " +
           "AND r.deletedAt IS NULL")
    Long countByProgramIdAndDeletedAtIsNull(@Param("programId") Integer programId);

    /**
     * 사용자가 이미 후기를 작성했는지 확인
     */
    @Query("SELECT COUNT(r) > 0 FROM ProgramReview r " +
           "WHERE r.user.userId = :userId " +
           "AND r.program.programId = :programId " +
           "AND r.deletedAt IS NULL")
    boolean existsByUserIdAndProgramIdAndDeletedAtIsNull(
            @Param("userId") Integer userId,
            @Param("programId") Integer programId);
}
