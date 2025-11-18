package com.scms.app.dto;

import com.scms.app.model.Portfolio;
import com.scms.app.model.PortfolioVisibility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 포트폴리오 상세 응답 DTO (상세보기용)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioDetailResponse {

    private Long portfolioId;
    private Integer userId;
    private String userName;
    private String title;
    private String description;
    private PortfolioVisibility visibility;
    private String templateType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 포함된 항목들
    private List<PortfolioItemResponse> items;

    // 통계 정보
    private Long totalViews;
    private Long itemCount;
    private Boolean hasActiveShare;

    /**
     * Entity를 DTO로 변환
     */
    public static PortfolioDetailResponse from(Portfolio portfolio) {
        return PortfolioDetailResponse.builder()
                .portfolioId(portfolio.getPortfolioId())
                .userId(portfolio.getUserId())
                .title(portfolio.getTitle())
                .description(portfolio.getDescription())
                .visibility(portfolio.getVisibility())
                .templateType(portfolio.getTemplateType())
                .createdAt(portfolio.getCreatedAt())
                .updatedAt(portfolio.getUpdatedAt())
                .build();
    }
}
