package com.scms.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 상담 상태 변경 요청 DTO (승인/거부)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationStatusUpdateRequest {

    private LocalDateTime scheduledDatetime; // 승인 시 확정 일시
    private String rejectionReason; // 거부 시 사유

    /**
     * 승인 요청 유효성 검증
     */
    public void validateForApproval() {
        if (scheduledDatetime == null) {
            throw new IllegalArgumentException("확정 상담 일시를 입력해주세요.");
        }

        if (scheduledDatetime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("과거 일시로는 상담을 확정할 수 없습니다.");
        }
    }

    /**
     * 거부 요청 유효성 검증
     */
    public void validateForRejection() {
        if (rejectionReason == null || rejectionReason.trim().isEmpty()) {
            throw new IllegalArgumentException("거부 사유를 입력해주세요.");
        }

        if (rejectionReason.length() > 500) {
            throw new IllegalArgumentException("거부 사유는 500자 이내로 작성해주세요.");
        }
    }
}
