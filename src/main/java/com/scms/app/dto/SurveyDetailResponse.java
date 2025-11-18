package com.scms.app.dto;

import com.scms.app.model.SurveyTargetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurveyDetailResponse {

    private Long surveyId;
    private String title;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isAnonymous;
    private Boolean isActive;
    private SurveyTargetType targetType;
    private Integer maxResponses;
    private Boolean allowMultipleResponses;
    private Boolean showResults;
    private Integer createdBy;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 추가 정보
    private Long responseCount;
    private Boolean isOngoing;
    private Boolean isExpired;
    private Boolean hasResponded;

    // 질문 목록
    private List<SurveyQuestionResponse> questions;
}
