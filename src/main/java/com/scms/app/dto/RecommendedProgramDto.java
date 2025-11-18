package com.scms.app.dto;

import com.scms.app.model.Program;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 추천 프로그램 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendedProgramDto {

    private Integer programId;
    private String title;
    private String description;
    private String category;
    private String subCategory;
    private LocalDateTime applicationStartDate;
    private LocalDateTime applicationEndDate;
    private LocalDateTime programStartDate;
    private LocalDateTime programEndDate;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private String thumbnailUrl;
    private String status;

    /**
     * 추천 점수 (0-100)
     */
    private Double recommendationScore;

    /**
     * 추천 이유 (향상시킬 수 있는 역량 목록)
     */
    private List<String> recommendationReasons;

    /**
     * Program 엔티티로부터 변환
     */
    public static RecommendedProgramDto from(Program program, Double score, List<String> reasons) {
        return RecommendedProgramDto.builder()
                .programId(program.getProgramId())
                .title(program.getTitle())
                .description(program.getDescription())
                .category(program.getCategory())
                .subCategory(program.getSubCategory())
                .applicationStartDate(program.getApplicationStartDate())
                .applicationEndDate(program.getApplicationEndDate())
                .programStartDate(program.getProgramStartDate())
                .programEndDate(program.getProgramEndDate())
                .maxParticipants(program.getMaxParticipants())
                .currentParticipants(program.getCurrentParticipants())
                .thumbnailUrl(program.getThumbnailUrl())
                .status(program.getStatus().name())
                .recommendationScore(score)
                .recommendationReasons(reasons)
                .build();
    }
}
