package com.scms.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurveyQuestionOptionResponse {

    private Long optionId;
    private Long questionId;
    private String optionText;
    private Integer displayOrder;
}
