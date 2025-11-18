package com.scms.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 학생 피드백 작성 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentFeedbackRequest {

    private String studentFeedback;
    private Integer satisfactionScore; // 1-5

    /**
     * 유효성 검증
     */
    public void validate() {
        if (satisfactionScore != null && (satisfactionScore < 1 || satisfactionScore > 5)) {
            throw new IllegalArgumentException("만족도는 1-5 사이의 값이어야 합니다.");
        }

        if (studentFeedback != null && studentFeedback.length() > 1000) {
            throw new IllegalArgumentException("피드백은 1000자 이내로 작성해주세요.");
        }
    }
}
