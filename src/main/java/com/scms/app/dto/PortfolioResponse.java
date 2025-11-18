package com.scms.app.dto;

import com.scms.app.model.Portfolio;
import com.scms.app.model.PortfolioVisibility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 포트폴리오 응답 DTO (목록용)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioResponse {

    private Long portfolioId;
    private Integer userId;
    private String title;
    private String description;
    private PortfolioVisibility visibility;
    private String templateType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 계산된 필드
    private Long itemCount;
    private Long viewCount;

    /**
     * Entity를 DTO로 변환
     */
    public static PortfolioResponse from(Portfolio portfolio) {
        return PortfolioResponse.builder()
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

    /**
     * Entity를 DTO로 변환 (카운트 포함)
     */
    public static PortfolioResponse from(Portfolio portfolio, Long itemCount, Long viewCount) {
        PortfolioResponse response = from(portfolio);
        response.setItemCount(itemCount);
        response.setViewCount(viewCount);
        return response;
    }
}
