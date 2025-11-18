package com.scms.app.dto;

import com.scms.app.model.PortfolioItem;
import com.scms.app.model.PortfolioItemType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 포트폴리오 항목 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioItemResponse {

    private Long itemId;
    private Long portfolioId;
    private PortfolioItemType itemType;
    private String title;
    private String description;
    private String organization;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer displayOrder;
    private Long programApplicationId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 연관 데이터
    private List<PortfolioFileResponse> files;

    /**
     * Entity를 DTO로 변환
     */
    public static PortfolioItemResponse from(PortfolioItem item) {
        return PortfolioItemResponse.builder()
                .itemId(item.getItemId())
                .portfolioId(item.getPortfolioId())
                .itemType(item.getItemType())
                .title(item.getTitle())
                .description(item.getDescription())
                .organization(item.getOrganization())
                .startDate(item.getStartDate())
                .endDate(item.getEndDate())
                .displayOrder(item.getDisplayOrder())
                .programApplicationId(item.getProgramApplicationId())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}
