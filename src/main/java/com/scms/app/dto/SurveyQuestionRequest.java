package com.scms.app.dto;

import com.scms.app.model.QuestionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SurveyQuestionRequest {

    @NotNull(message = "질문 유형은 필수입니다")
    private QuestionType questionType;

    @NotBlank(message = "질문 내용은 필수입니다")
    @Size(max = 2000, message = "질문 내용은 2000자 이내로 입력해주세요")
    private String questionText;

    @NotNull(message = "필수 응답 여부는 필수입니다")
    private Boolean isRequired = true;

    @Min(value = 0, message = "표시 순서는 0 이상이어야 합니다")
    private Integer displayOrder = 0;

    // 척도형 질문용
    @Min(value = 1, message = "척도 최소값은 1 이상이어야 합니다")
    private Integer scaleMin;

    @Max(value = 10, message = "척도 최대값은 10 이하여야 합니다")
    private Integer scaleMax;

    @Size(max = 50, message = "척도 레이블은 50자 이내로 입력해주세요")
    private String scaleMinLabel;

    @Size(max = 50, message = "척도 레이블은 50자 이내로 입력해주세요")
    private String scaleMaxLabel;

    // 객관식 질문용
    @Valid
    private List<SurveyQuestionOptionRequest> options = new ArrayList<>();

    /**
     * 객관식 질문 유효성 검증
     */
    @AssertTrue(message = "객관식 질문은 최소 2개 이상의 선택지가 필요합니다")
    public boolean isValidChoiceQuestion() {
        if (questionType == QuestionType.SINGLE_CHOICE ||
            questionType == QuestionType.MULTIPLE_CHOICE) {
            return options != null && options.size() >= 2;
        }
        return true;
    }

    /**
     * 척도형 질문 유효성 검증
     */
    @AssertTrue(message = "척도형 질문은 최소값과 최대값이 필요합니다")
    public boolean isValidScaleQuestion() {
        if (questionType == QuestionType.SCALE) {
            return scaleMin != null && scaleMax != null && scaleMin < scaleMax;
        }
        return true;
    }
}
