package com.scms.app.dto;

import com.scms.app.model.StudentCompetencyAssessment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 역량 평가 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssessmentResponse {

    private Long id;
    private Long studentId;
    private String studentName;
    private String studentNumber;
    private Long competencyId;
    private String competencyName;
    private Long categoryId;
    private String categoryName;
    private Integer score;
    private String grade;
    private String scoreLevel;
    private LocalDate assessmentDate;
    private String assessor;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Entity를 DTO로 변환
     */
    public static AssessmentResponse from(StudentCompetencyAssessment assessment) {
        return AssessmentResponse.builder()
                .id(assessment.getId())
                .studentId(assessment.getStudent().getId())
                .studentName(assessment.getStudent().getName())
                .studentNumber(assessment.getStudent().getStudentId())
                .competencyId(assessment.getCompetency().getId())
                .competencyName(assessment.getCompetency().getName())
                .categoryId(assessment.getCompetency().getCategory().getId())
                .categoryName(assessment.getCompetency().getCategory().getName())
                .score(assessment.getScore())
                .grade(assessment.getGrade())
                .scoreLevel(assessment.getScoreLevel())
                .assessmentDate(assessment.getAssessmentDate())
                .assessor(assessment.getAssessor())
                .notes(assessment.getNotes())
                .createdAt(assessment.getCreatedAt())
                .updatedAt(assessment.getUpdatedAt())
                .build();
    }
}
