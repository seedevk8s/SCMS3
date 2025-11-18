package com.scms.app.dto;

import com.scms.app.model.SurveyTargetType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class SurveyCreateRequest {

    @NotBlank(message = "설문 제목은 필수입니다")
    @Size(max = 200, message = "제목은 200자 이내로 입력해주세요")
    private String title;

    @Size(max = 5000, message = "설명은 5000자 이내로 입력해주세요")
    private String description;

    @NotNull(message = "시작일시는 필수입니다")
    @Future(message = "시작일시는 현재 이후여야 합니다")
    private LocalDateTime startDate;

    @NotNull(message = "종료일시는 필수입니다")
    @Future(message = "종료일시는 현재 이후여야 합니다")
    private LocalDateTime endDate;

    @NotNull(message = "익명 여부는 필수입니다")
    private Boolean isAnonymous = false;

    @NotNull(message = "대상 유형은 필수입니다")
    private SurveyTargetType targetType = SurveyTargetType.ALL;

    @Min(value = 1, message = "최대 응답 수는 1 이상이어야 합니다")
    private Integer maxResponses;

    private Boolean allowMultipleResponses = false;

    private Boolean showResults = false;

    @NotEmpty(message = "질문은 최소 1개 이상 등록해야 합니다")
    @Valid
    private List<SurveyQuestionRequest> questions = new ArrayList<>();

    private List<Integer> targetUserIds = new ArrayList<>();  // SPECIFIC 유형용

    /**
     * 날짜 유효성 검증
     */
    @AssertTrue(message = "종료일시는 시작일시 이후여야 합니다")
    public boolean isValidDateRange() {
        if (startDate == null || endDate == null) {
            return true;  // @NotNull에서 처리
        }
        return endDate.isAfter(startDate);
    }
}
