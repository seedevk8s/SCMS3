package com.scms.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 학생 역량 평가 엔티티
 * 학생의 역량별 평가 결과를 저장
 */
@Entity
@Table(name = "student_competency_assessments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentCompetencyAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competency_id", nullable = false)
    private Competency competency;

    @Column(nullable = false)
    private Integer score;  // 0-100

    @Column(name = "assessment_date", nullable = false)
    private LocalDate assessmentDate;

    @Column(length = 100)
    private String assessor;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (assessmentDate == null) {
            assessmentDate = LocalDate.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * 점수 유효성 검증
     */
    public boolean isValidScore() {
        return score != null && score >= 0 && score <= 100;
    }

    /**
     * 등급 계산 (A~F)
     */
    public String getGrade() {
        if (score == null) return "N/A";
        if (score >= 90) return "A";
        if (score >= 80) return "B";
        if (score >= 70) return "C";
        if (score >= 60) return "D";
        return "F";
    }

    /**
     * 점수 레벨 (우수/보통/미흡)
     */
    public String getScoreLevel() {
        if (score == null) return "미평가";
        if (score >= 80) return "우수";
        if (score >= 60) return "보통";
        return "미흡";
    }
}
