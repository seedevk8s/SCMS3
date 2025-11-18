package com.scms.app.dto;

import com.scms.app.model.Competency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 역량 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompetencyResponse {

    private Long id;
    private Long categoryId;
    private String categoryName;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Entity를 DTO로 변환
     */
    public static CompetencyResponse from(Competency competency) {
        return CompetencyResponse.builder()
                .id(competency.getId())
                .categoryId(competency.getCategory().getId())
                .categoryName(competency.getCategory().getName())
                .name(competency.getName())
                .description(competency.getDescription())
                .createdAt(competency.getCreatedAt())
                .updatedAt(competency.getUpdatedAt())
                .build();
    }
}
