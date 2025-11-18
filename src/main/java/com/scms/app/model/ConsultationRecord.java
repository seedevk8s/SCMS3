package com.scms.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 상담 기록 엔티티
 */
@Entity
@Table(name = "consultation_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private Long recordId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ConsultationSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counselor_id", nullable = false)
    private Counselor counselor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(name = "consultation_date", nullable = false)
    private LocalDateTime consultationDate;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "counselor_notes", columnDefinition = "TEXT")
    private String counselorNotes;

    @Column(name = "student_feedback", columnDefinition = "TEXT")
    private String studentFeedback;

    @Column(name = "satisfaction_score")
    private Integer satisfactionScore;

    @Column(name = "follow_up_required")
    private Boolean followUpRequired = false;

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
     * 만족도 유효성 검증 (1~5점)
     */
    public boolean isValidSatisfactionScore() {
        return satisfactionScore != null && satisfactionScore >= 1 && satisfactionScore <= 5;
    }
}
