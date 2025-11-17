package com.scms.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 마일리지 적립 내역 엔티티
 * - 학생별 마일리지 지급/차감 이력 관리
 */
@Entity
@Table(name = "mileage_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MileageHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "activity_type", nullable = false, length = 50)
    private String activityType;

    @Column(name = "activity_id")
    private Long activityId;

    @Column(name = "activity_name", nullable = false, length = 255)
    private String activityName;

    @Column(name = "points", nullable = false)
    private Integer points;

    @Column(name = "description", length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "awarded_by")
    private User awardedBy;

    @Column(name = "earned_at", nullable = false)
    private LocalDateTime earnedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.earnedAt == null) {
            this.earnedAt = LocalDateTime.now();
        }
    }

    /**
     * 마일리지 적립 타입인지 확인
     */
    public boolean isEarned() {
        return this.points > 0;
    }

    /**
     * 마일리지 차감 타입인지 확인
     */
    public boolean isDeducted() {
        return this.points < 0;
    }
}
