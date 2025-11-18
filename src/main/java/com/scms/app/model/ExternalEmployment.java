package com.scms.app.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

/**
 * 외부취업 활동 엔티티
 * 학생들의 인턴십, 현장실습, 외부 프로젝트, 취업, 창업 등의 활동을 관리
 */
@Entity
@Table(name = "external_employments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExternalEmployment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employment_id")
    private Long employmentId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", nullable = false, length = 50)
    private EmploymentType employmentType;

    @Column(name = "company_name", nullable = false, length = 200)
    private String companyName;

    @Column(name = "position", length = 100)
    private String position;

    @Column(name = "department", length = 100)
    private String department;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "duration_months")
    private Integer durationMonths;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "work_content", columnDefinition = "TEXT")
    private String workContent;

    @Column(name = "skills_learned", columnDefinition = "TEXT")
    private String skillsLearned;

    @Column(name = "certificate_url", length = 500)
    private String certificateUrl;

    @Column(name = "credits", nullable = false)
    private Integer credits = 0;

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;

    @Column(name = "verified_by")
    private Integer verifiedBy;

    @Column(name = "verification_date")
    private LocalDateTime verificationDate;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "is_portfolio_linked", nullable = false)
    private Boolean isPortfolioLinked = false;

    @Column(name = "portfolio_item_id")
    private Long portfolioItemId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        // 기간(개월) 자동 계산
        if (startDate != null && endDate != null) {
            Period period = Period.between(startDate, endDate);
            durationMonths = period.getYears() * 12 + period.getMonths();
        } else if (startDate != null) {
            // 종료일이 없으면 현재까지의 기간 계산
            Period period = Period.between(startDate, LocalDate.now());
            durationMonths = period.getYears() * 12 + period.getMonths();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();

        // 기간(개월) 자동 계산
        if (startDate != null && endDate != null) {
            Period period = Period.between(startDate, endDate);
            durationMonths = period.getYears() * 12 + period.getMonths();
        } else if (startDate != null) {
            Period period = Period.between(startDate, LocalDate.now());
            durationMonths = period.getYears() * 12 + period.getMonths();
        }
    }

    /**
     * Soft Delete
     */
    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * 삭제되지 않은 상태인지 확인
     */
    public boolean isActive() {
        return deletedAt == null;
    }

    /**
     * 진행 중인 활동인지 확인
     */
    public boolean isOngoing() {
        if (endDate == null) {
            return true; // 종료일이 없으면 진행 중
        }
        return LocalDate.now().isBefore(endDate) || LocalDate.now().isEqual(endDate);
    }

    /**
     * 활동 승인
     */
    public void approve(Integer verifiedBy, Integer credits) {
        this.isVerified = true;
        this.verifiedBy = verifiedBy;
        this.verificationDate = LocalDateTime.now();
        this.credits = credits;
        this.rejectionReason = null;
    }

    /**
     * 활동 거절
     */
    public void reject(Integer verifiedBy, String rejectionReason) {
        this.isVerified = false;
        this.verifiedBy = verifiedBy;
        this.verificationDate = LocalDateTime.now();
        this.rejectionReason = rejectionReason;
        this.credits = 0;
    }

    /**
     * 포트폴리오 연동
     */
    public void linkToPortfolio(Long portfolioItemId) {
        this.isPortfolioLinked = true;
        this.portfolioItemId = portfolioItemId;
    }

    /**
     * 포트폴리오 연동 해제
     */
    public void unlinkFromPortfolio() {
        this.isPortfolioLinked = false;
        this.portfolioItemId = null;
    }
}
