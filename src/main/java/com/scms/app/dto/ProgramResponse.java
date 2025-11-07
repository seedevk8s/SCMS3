package com.scms.app.dto;

import com.scms.app.model.Program;
import com.scms.app.model.ProgramStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 프로그램 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgramResponse {

    private Integer programId;
    private String title;
    private String description;
    private String content;
    private String department;
    private String college;
    private String category;
    private String subCategory;
    private LocalDateTime applicationStartDate;
    private LocalDateTime applicationEndDate;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private String thumbnailUrl;
    private Integer hits;
    private ProgramStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 계산된 필드
    private Long dDay;
    private Integer participationRate;
    private Boolean isApplicationAvailable;

    /**
     * Entity를 DTO로 변환
     */
    public static ProgramResponse from(Program program) {
        return ProgramResponse.builder()
                .programId(program.getProgramId())
                .title(program.getTitle())
                .description(program.getDescription())
                .content(program.getContent())
                .department(program.getDepartment())
                .college(program.getCollege())
                .category(program.getCategory())
                .subCategory(program.getSubCategory())
                .applicationStartDate(program.getApplicationStartDate())
                .applicationEndDate(program.getApplicationEndDate())
                .maxParticipants(program.getMaxParticipants())
                .currentParticipants(program.getCurrentParticipants())
                .thumbnailUrl(program.getThumbnailUrl())
                .hits(program.getHits())
                .status(program.getStatus())
                .createdAt(program.getCreatedAt())
                .updatedAt(program.getUpdatedAt())
                .dDay(program.getDDay())
                .participationRate(program.getParticipationRate())
                .isApplicationAvailable(program.isApplicationAvailable())
                .build();
    }
}
