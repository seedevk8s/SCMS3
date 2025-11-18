package com.scms.app.dto;

import com.scms.app.model.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurveyQuestionResponse {

    private Long questionId;
    private Long surveyId;
    private QuestionType questionType;
    private String questionText;
    private Boolean isRequired;
    private Integer displayOrder;
    private Integer scaleMin;
    private Integer scaleMax;
    private String scaleMinLabel;
    private String scaleMaxLabel;

    // 객관식 선택지 목록
    private List<SurveyQuestionOptionResponse> options;
}
