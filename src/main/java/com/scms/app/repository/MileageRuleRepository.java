package com.scms.app.repository;

import com.scms.app.model.MileageRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 마일리지 규칙 Repository
 */
@Repository
public interface MileageRuleRepository extends JpaRepository<MileageRule, Long> {

    /**
     * 활성화된 모든 규칙 조회 (삭제되지 않은 것만)
     */
    @Query("SELECT r FROM MileageRule r " +
           "WHERE r.isActive = true " +
           "AND r.deletedAt IS NULL " +
           "ORDER BY r.activityType, r.points DESC")
    List<MileageRule> findActiveRules();

    /**
     * 특정 활동 타입의 활성화된 규칙 조회
     */
    @Query("SELECT r FROM MileageRule r " +
           "WHERE r.activityType = :activityType " +
           "AND r.isActive = true " +
           "AND r.deletedAt IS NULL " +
           "ORDER BY r.points DESC")
    List<MileageRule> findByActivityTypeAndActive(@Param("activityType") String activityType);

    /**
     * 모든 규칙 조회 (삭제되지 않은 것만, 관리자용)
     */
    @Query("SELECT r FROM MileageRule r " +
           "WHERE r.deletedAt IS NULL " +
           "ORDER BY r.createdAt DESC")
    List<MileageRule> findAllNotDeleted();

    /**
     * 특정 규칙 ID로 조회 (삭제되지 않은 것만)
     */
    @Query("SELECT r FROM MileageRule r " +
           "WHERE r.ruleId = :ruleId " +
           "AND r.deletedAt IS NULL")
    Optional<MileageRule> findByIdNotDeleted(@Param("ruleId") Long ruleId);

    /**
     * 활동 타입별 그룹핑 (통계용)
     */
    @Query("SELECT r.activityType, COUNT(r) FROM MileageRule r " +
           "WHERE r.isActive = true " +
           "AND r.deletedAt IS NULL " +
           "GROUP BY r.activityType")
    List<Object[]> countByActivityType();
}
