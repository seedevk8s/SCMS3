package com.scms.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * 역량 평가 리포트 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssessmentReportResponse {

    private Long studentId;
    private String studentName;
    private String studentNumber;
    private Double totalScore;
    private String overallGrade;
    private LocalDate latestAssessmentDate;
    private Integer assessmentCount;

    private List<CategoryScoreDto> categoryScores;
    private List<CompetencyScoreDto> strengths;  // Top 3 강점
    private List<CompetencyScoreDto> weaknesses;  // Bottom 3 약점
    private List<RecommendedProgramDto> recommendations;

    /**
     * 카테고리별 점수 DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryScoreDto {
        private Long categoryId;
        private String categoryName;
        private Double averageScore;
        private String grade;
        private List<CompetencyScoreDto> competencies;
    }

    /**
     * 역량별 점수 DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CompetencyScoreDto {
        private Long competencyId;
        private String competencyName;
        private Long categoryId;
        private String categoryName;
        private Integer score;
        private String grade;
        private String scoreLevel;
    }

    /**
     * 추천 프로그램 DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecommendedProgramDto {
        private Integer programId;
        private String programName;
        private String category;
        private String reason;
        private String relatedCompetency;
    }
}
