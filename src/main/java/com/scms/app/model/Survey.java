package com.scms.app.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 설문조사 엔티티
 */
@Entity
@Table(name = "surveys")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Survey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "survey_id")
    private Long surveyId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "is_anonymous", nullable = false)
    private Boolean isAnonymous = false;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private SurveyTargetType targetType = SurveyTargetType.ALL;

    @Column(name = "max_responses")
    private Integer maxResponses;

    @Column(name = "allow_multiple_responses", nullable = false)
    private Boolean allowMultipleResponses = false;

    @Column(name = "show_results", nullable = false)
    private Boolean showResults = false;

    @Column(name = "created_by", nullable = false)
    private Integer createdBy;

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
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Soft Delete
     */
    public void delete() {
        this.deletedAt = LocalDateTime.now();
        this.isActive = false;
    }

    /**
     * 설문이 현재 진행 중인지 확인
     */
    public boolean isOngoing() {
        LocalDateTime now = LocalDateTime.now();
        return isActive &&
               deletedAt == null &&
               !now.isBefore(startDate) &&
               !now.isAfter(endDate);
    }

    /**
     * 설문이 종료되었는지 확인
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(endDate);
    }

    /**
     * 응답 가능 여부 확인
     */
    public boolean isAvailableForResponse() {
        return isOngoing();
    }
}
