package com.scms.app.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SurveyQuestionOptionRequest {

    @NotBlank(message = "선택지 내용은 필수입니다")
    @Size(max = 500, message = "선택지 내용은 500자 이내로 입력해주세요")
    private String optionText;

    @Min(value = 0, message = "표시 순서는 0 이상이어야 합니다")
    private Integer displayOrder = 0;
}
