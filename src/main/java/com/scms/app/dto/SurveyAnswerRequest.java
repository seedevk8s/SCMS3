package com.scms.app.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class SurveyAnswerRequest {

    @NotNull(message = "질문 ID는 필수입니다")
    private Long questionId;

    // 객관식 단일선택용
    private Long optionId;

    // 객관식 복수선택용
    private List<Long> optionIds;

    // 주관식용
    @Size(max = 5000, message = "주관식 답변은 5000자 이내로 입력해주세요")
    private String answerText;

    // 척도형용
    @Min(value = 1, message = "척도 값은 1 이상이어야 합니다")
    @Max(value = 10, message = "척도 값은 10 이하여야 합니다")
    private Integer answerNumber;
}
