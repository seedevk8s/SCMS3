package com.scms.app.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SurveySubmitRequest {

    @NotNull(message = "설문 ID는 필수입니다")
    private Long surveyId;

    @NotEmpty(message = "답변은 최소 1개 이상 제출해야 합니다")
    @Valid
    private List<SurveyAnswerRequest> answers = new ArrayList<>();
}
