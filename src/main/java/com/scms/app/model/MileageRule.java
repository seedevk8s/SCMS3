package com.scms.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 마일리지 규칙 엔티티
 * - 활동별 마일리지 지급 기준 관리
 */
@Entity
@Table(name = "mileage_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MileageRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rule_id")
    private Long ruleId;

    @Column(name = "activity_type", nullable = false, length = 50)
    private String activityType;

    @Column(name = "activity_name", nullable = false, length = 100)
    private String activityName;

    @Column(name = "points", nullable = false)
    private Integer points;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Soft Delete
     */
    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * 삭제 여부 확인
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    /**
     * 활성화
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * 비활성화
     */
    public void deactivate() {
        this.isActive = false;
    }
}
