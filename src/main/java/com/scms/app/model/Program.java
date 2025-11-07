package com.scms.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 비교과 프로그램 엔티티
 */
@Entity
@Table(name = "programs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Program {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "program_id")
    private Integer programId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "department", length = 100)
    private String department;

    @Column(name = "college", length = 100)
    private String college;

    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Column(name = "sub_category", length = 50)
    private String subCategory;

    @Column(name = "application_start_date", nullable = false)
    private LocalDateTime applicationStartDate;

    @Column(name = "application_end_date", nullable = false)
    private LocalDateTime applicationEndDate;

    @Column(name = "max_participants")
    private Integer maxParticipants;

    @Column(name = "current_participants", nullable = false)
    private Integer currentParticipants = 0;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(name = "hits", nullable = false)
    private Integer hits = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private ProgramStatus status = ProgramStatus.SCHEDULED;

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
     * 조회수 증가
     */
    public void incrementHits() {
        this.hits++;
    }

    /**
     * 참가자수 증가
     */
    public boolean incrementParticipants() {
        if (this.maxParticipants != null && this.currentParticipants >= this.maxParticipants) {
            return false;
        }
        this.currentParticipants++;
        return true;
    }

    /**
     * 참가자수 감소
     */
    public void decrementParticipants() {
        if (this.currentParticipants > 0) {
            this.currentParticipants--;
        }
    }

    /**
     * 신청 가능 여부 확인
     */
    public boolean isApplicationAvailable() {
        LocalDateTime now = LocalDateTime.now();
        return status == ProgramStatus.OPEN
            && now.isAfter(applicationStartDate)
            && now.isBefore(applicationEndDate)
            && (maxParticipants == null || currentParticipants < maxParticipants)
            && !isDeleted();
    }

    /**
     * 신청 마감 여부 확인
     */
    public boolean isApplicationClosed() {
        return status == ProgramStatus.CLOSED
            || LocalDateTime.now().isAfter(applicationEndDate)
            || (maxParticipants != null && currentParticipants >= maxParticipants);
    }

    /**
     * D-day 계산 (신청 종료일 기준)
     */
    public Long getDDay() {
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(applicationEndDate)) {
            return null;
        }
        return java.time.Duration.between(now, applicationEndDate).toDays();
    }

    /**
     * 참가율 계산 (%)
     */
    public Integer getParticipationRate() {
        if (maxParticipants == null || maxParticipants == 0) {
            return 0;
        }
        return (currentParticipants * 100) / maxParticipants;
    }
}
