package com.scms.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 상담사 근무 일정 엔티티
 */
@Entity
@Table(name = "counselor_schedules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CounselorSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Long scheduleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counselor_id", nullable = false)
    private Counselor counselor;

    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek; // 1=월요일, 7=일요일

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = true;

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
     * 요일 유효성 검증 (1~7)
     */
    public boolean isValidDayOfWeek() {
        return dayOfWeek != null && dayOfWeek >= 1 && dayOfWeek <= 7;
    }

    /**
     * 요일 이름 반환
     */
    public String getDayOfWeekName() {
        if (dayOfWeek == null) return "";
        return switch (dayOfWeek) {
            case 1 -> "월요일";
            case 2 -> "화요일";
            case 3 -> "수요일";
            case 4 -> "목요일";
            case 5 -> "금요일";
            case 6 -> "토요일";
            case 7 -> "일요일";
            default -> "";
        };
    }

    /**
     * 시간 유효성 검증 (시작시간 < 종료시간)
     */
    public boolean isValidTimeRange() {
        return startTime != null && endTime != null && startTime.isBefore(endTime);
    }
}
