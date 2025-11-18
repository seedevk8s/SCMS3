package com.scms.app.repository;

import com.scms.app.model.ExternalEmployment;
import com.scms.app.model.EmploymentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 외부취업 활동 Repository
 */
@Repository
public interface ExternalEmploymentRepository extends JpaRepository<ExternalEmployment, Long> {

    /**
     * 사용자 ID로 조회 (삭제되지 않은 것만)
     */
    @Query("SELECT e FROM ExternalEmployment e WHERE e.userId = :userId AND e.deletedAt IS NULL ORDER BY e.createdAt DESC")
    List<ExternalEmployment> findByUserId(@Param("userId") Integer userId);

    /**
     * 사용자 ID와 승인 여부로 조회
     */
    @Query("SELECT e FROM ExternalEmployment e WHERE e.userId = :userId AND e.isVerified = :isVerified AND e.deletedAt IS NULL ORDER BY e.createdAt DESC")
    List<ExternalEmployment> findByUserIdAndIsVerified(@Param("userId") Integer userId, @Param("isVerified") Boolean isVerified);

    /**
     * 승인 대기 중인 신청 조회 (관리자용)
     */
    @Query("SELECT e FROM ExternalEmployment e WHERE e.isVerified = false AND e.deletedAt IS NULL ORDER BY e.createdAt ASC")
    Page<ExternalEmployment> findPendingEmployments(Pageable pageable);

    /**
     * 승인된 신청 조회 (관리자용)
     */
    @Query("SELECT e FROM ExternalEmployment e WHERE e.isVerified = true AND e.deletedAt IS NULL ORDER BY e.verificationDate DESC")
    Page<ExternalEmployment> findVerifiedEmployments(Pageable pageable);

    /**
     * 활동 유형별 조회
     */
    @Query("SELECT e FROM ExternalEmployment e WHERE e.employmentType = :type AND e.deletedAt IS NULL ORDER BY e.createdAt DESC")
    List<ExternalEmployment> findByEmploymentType(@Param("type") EmploymentType type);

    /**
     * ID로 조회 (삭제되지 않은 것만)
     */
    @Query("SELECT e FROM ExternalEmployment e WHERE e.employmentId = :id AND e.deletedAt IS NULL")
    Optional<ExternalEmployment> findByIdAndNotDeleted(@Param("id") Long id);

    /**
     * 사용자의 총 획득 가점 계산
     */
    @Query("SELECT COALESCE(SUM(e.credits), 0) FROM ExternalEmployment e WHERE e.userId = :userId AND e.isVerified = true AND e.deletedAt IS NULL")
    Integer getTotalCreditsByUserId(@Param("userId") Integer userId);

    /**
     * 사용자의 승인된 활동 개수
     */
    @Query("SELECT COUNT(e) FROM ExternalEmployment e WHERE e.userId = :userId AND e.isVerified = true AND e.deletedAt IS NULL")
    Long countVerifiedByUserId(@Param("userId") Integer userId);

    /**
     * 활동 유형별 통계 (관리자용)
     */
    @Query("SELECT e.employmentType, COUNT(e) FROM ExternalEmployment e WHERE e.isVerified = true AND e.deletedAt IS NULL GROUP BY e.employmentType")
    List<Object[]> getStatisticsByType();

    /**
     * 월별 신청 통계 (관리자용)
     */
    @Query(value = "SELECT TO_CHAR(created_at, 'YYYY-MM') as month, COUNT(*) as count " +
            "FROM external_employments " +
            "WHERE deleted_at IS NULL " +
            "GROUP BY TO_CHAR(created_at, 'YYYY-MM') " +
            "ORDER BY month DESC " +
            "LIMIT 12", nativeQuery = true)
    List<Object[]> getMonthlyStatistics();

    /**
     * 전체 신청 수 (삭제되지 않은 것만)
     */
    @Query("SELECT COUNT(e) FROM ExternalEmployment e WHERE e.deletedAt IS NULL")
    Long countAllActive();

    /**
     * 승인 대기 중인 신청 수
     */
    @Query("SELECT COUNT(e) FROM ExternalEmployment e WHERE e.isVerified = false AND e.deletedAt IS NULL")
    Long countPending();
}
