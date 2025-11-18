package com.scms.app.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 외부취업 가점 규칙 엔티티
 * 활동 유형 및 기간에 따른 가점 기준을 관리
 */
@Entity
@Table(name = "external_employment_credit_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExternalEmploymentCreditRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rule_id")
    private Integer ruleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", nullable = false, length = 50)
    private EmploymentType employmentType;

    @Column(name = "min_duration_months")
    private Integer minDurationMonths;

    @Column(name = "max_duration_months")
    private Integer maxDurationMonths;

    @Column(name = "base_credits", nullable = false)
    private Integer baseCredits;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * 주어진 기간(개월)이 이 규칙에 해당하는지 확인
     */
    public boolean matches(Integer durationMonths) {
        if (durationMonths == null) {
            return false;
        }

        boolean minMatch = (minDurationMonths == null || durationMonths >= minDurationMonths);
        boolean maxMatch = (maxDurationMonths == null || durationMonths <= maxDurationMonths);

        return minMatch && maxMatch;
    }

    /**
     * 규칙 비활성화
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * 규칙 활성화
     */
    public void activate() {
        this.isActive = true;
    }
}
