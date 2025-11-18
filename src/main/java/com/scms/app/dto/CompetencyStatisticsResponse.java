package com.scms.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 역량별 통계 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompetencyStatisticsResponse {

    private Long competencyId;
    private String competencyName;
    private Long categoryId;
    private String categoryName;

    private Long totalAssessments;
    private Double averageScore;
    private Integer maxScore;
    private Integer minScore;

    private Long excellentCount;  // 80점 이상
    private Long goodCount;       // 60-79점
    private Long needsImprovementCount;  // 60점 미만
}
