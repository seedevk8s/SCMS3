package com.scms.app.dto;

import com.scms.app.model.ConsultationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 상담 신청 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationRequest {

    private Integer counselorId; // null이면 자동 배정
    private ConsultationType consultationType;
    private LocalDate requestedDate;
    private LocalTime requestedTime;
    private String title;
    private String content;

    /**
     * 유효성 검증
     */
    public void validate() {
        if (consultationType == null) {
            throw new IllegalArgumentException("상담 유형을 선택해주세요.");
        }

        if (requestedDate == null) {
            throw new IllegalArgumentException("희망 상담 날짜를 선택해주세요.");
        }

        if (requestedDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("과거 날짜로는 상담을 신청할 수 없습니다.");
        }

        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("상담 제목을 입력해주세요.");
        }

        if (title.length() > 200) {
            throw new IllegalArgumentException("상담 제목은 200자 이내로 작성해주세요.");
        }

        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("상담 내용을 입력해주세요.");
        }

        if (content.length() > 2000) {
            throw new IllegalArgumentException("상담 내용은 2000자 이내로 작성해주세요.");
        }
    }
}
