package com.scms.app.dto;

import com.scms.app.model.SurveyTargetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurveyDTO {

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
    private String createdByName;  // 생성자 이름
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 추가 정보
    private Long questionCount;     // 질문 수
    private Long responseCount;     // 응답 수
    private Boolean isOngoing;      // 진행 중 여부
    private Boolean isExpired;      // 만료 여부
    private Boolean hasResponded;   // 현재 사용자 응답 여부
}
