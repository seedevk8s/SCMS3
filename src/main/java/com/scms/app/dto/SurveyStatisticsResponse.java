package com.scms.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurveyStatisticsResponse {

    private Long surveyId;
    private String title;
    private Long totalResponses;      // 전체 응답 수
    private Long targetCount;         // 대상자 수 (SPECIFIC 유형인 경우)
    private Double responseRate;      // 응답률 (%)

    // 질문별 통계
    private List<QuestionStatisticsResponse> questionStatistics;
}
