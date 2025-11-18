package com.scms.app.dto;

import com.scms.app.model.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionStatisticsResponse {

    private Long questionId;
    private String questionText;
    private QuestionType questionType;
    private Long totalAnswers;        // 총 응답 수

    // 객관식 통계
    private List<OptionStatistics> optionStatistics;

    // 척도형 통계
    private Double averageScale;      // 평균 점수
    private Map<Integer, Long> scaleDistribution;  // 척도별 분포

    // 주관식 응답 목록
    private List<String> textAnswers;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionStatistics {
        private Long optionId;
        private String optionText;
        private Long count;
        private Double percentage;
    }
}
