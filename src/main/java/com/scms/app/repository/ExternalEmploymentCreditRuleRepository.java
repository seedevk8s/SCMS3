package com.scms.app.repository;

import com.scms.app.model.ExternalEmploymentCreditRule;
import com.scms.app.model.EmploymentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 외부취업 가점 규칙 Repository
 */
@Repository
public interface ExternalEmploymentCreditRuleRepository extends JpaRepository<ExternalEmploymentCreditRule, Integer> {

    /**
     * 활성화된 규칙만 조회
     */
    @Query("SELECT r FROM ExternalEmploymentCreditRule r WHERE r.isActive = true ORDER BY r.employmentType, r.minDurationMonths")
    List<ExternalEmploymentCreditRule> findAllActive();

    /**
     * 활동 유형별 활성화된 규칙 조회
     */
    @Query("SELECT r FROM ExternalEmploymentCreditRule r WHERE r.employmentType = :type AND r.isActive = true ORDER BY r.minDurationMonths")
    List<ExternalEmploymentCreditRule> findByEmploymentTypeAndActive(@Param("type") EmploymentType type);

    /**
     * 활동 유형과 기간에 맞는 규칙 찾기
     */
    @Query("SELECT r FROM ExternalEmploymentCreditRule r " +
            "WHERE r.employmentType = :type " +
            "AND r.isActive = true " +
            "AND (r.minDurationMonths IS NULL OR r.minDurationMonths <= :durationMonths) " +
            "AND (r.maxDurationMonths IS NULL OR r.maxDurationMonths >= :durationMonths) " +
            "ORDER BY r.baseCredits DESC")
    List<ExternalEmploymentCreditRule> findMatchingRules(
            @Param("type") EmploymentType type,
            @Param("durationMonths") Integer durationMonths
    );
}
