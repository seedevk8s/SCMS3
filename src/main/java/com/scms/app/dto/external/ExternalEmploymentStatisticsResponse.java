package com.scms.app.dto.external;

import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * 외부취업 활동 통계 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExternalEmploymentStatisticsResponse {

    private Long totalCount; // 전체 신청 수
    private Long pendingCount; // 승인 대기 중
    private Long verifiedCount; // 승인 완료
    private Integer totalCredits; // 총 가점 부여

    // 활동 유형별 통계
    private Map<String, Long> countByType;

    // 월별 신청 통계 (최근 12개월)
    private List<MonthlyStatistic> monthlyStatistics;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MonthlyStatistic {
        private String month; // YYYY-MM
        private Long count;
    }
}
