package com.scms.app.dto;

import com.scms.app.model.CompetencyCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 역량 카테고리 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompetencyCategoryResponse {

    private Long id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CompetencyResponse> competencies;

    /**
     * Entity를 DTO로 변환 (역량 목록 포함)
     */
    public static CompetencyCategoryResponse fromWithCompetencies(CompetencyCategory category) {
        return CompetencyCategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .competencies(category.getCompetencies().stream()
                        .map(CompetencyResponse::from)
                        .collect(Collectors.toList()))
                .build();
    }

    /**
     * Entity를 DTO로 변환 (역량 목록 제외)
     */
    public static CompetencyCategoryResponse from(CompetencyCategory category) {
        return CompetencyCategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
