package com.scms.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 상담 세션 엔티티 (상담 신청 및 예약)
 */
@Entity
@Table(name = "consultation_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    private Long sessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counselor_id")
    private Counselor counselor;

    @Enumerated(EnumType.STRING)
    @Column(name = "consultation_type", length = 50, nullable = false)
    private ConsultationType consultationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private ConsultationStatus status = ConsultationStatus.PENDING;

    @Column(name = "requested_date", nullable = false)
    private LocalDate requestedDate;

    @Column(name = "requested_time")
    private LocalTime requestedTime;

    @Column(name = "scheduled_datetime")
    private LocalDateTime scheduledDatetime;

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

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
     * 상담 승인
     */
    public void approve(LocalDateTime scheduledDatetime) {
        this.status = ConsultationStatus.APPROVED;
        this.scheduledDatetime = scheduledDatetime;
    }

    /**
     * 상담 거부
     */
    public void reject(String reason) {
        this.status = ConsultationStatus.REJECTED;
        this.rejectionReason = reason;
    }

    /**
     * 상담 취소
     */
    public void cancel() {
        this.status = ConsultationStatus.CANCELLED;
    }

    /**
     * 상담 완료 처리
     */
    public void complete() {
        this.status = ConsultationStatus.COMPLETED;
    }

    /**
     * 취소 가능 여부 확인
     */
    public boolean isCancellable() {
        return (status == ConsultationStatus.PENDING || status == ConsultationStatus.APPROVED)
                && !isDeleted();
    }

    /**
     * 승인/거부 가능 여부 확인 (상담사용)
     */
    public boolean isApprovable() {
        return status == ConsultationStatus.PENDING && !isDeleted();
    }
}
