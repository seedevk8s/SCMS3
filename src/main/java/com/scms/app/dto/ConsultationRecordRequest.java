package com.scms.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 상담 기록 작성/수정 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationRecordRequest {

    private LocalDateTime consultationDate;
    private Integer durationMinutes;
    private String summary;
    private String counselorNotes;
    private Boolean followUpRequired;

    /**
     * 유효성 검증
     */
    public void validate() {
        if (consultationDate == null) {
            throw new IllegalArgumentException("상담 일시를 입력해주세요.");
        }

        if (consultationDate.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("미래 일시로는 상담 기록을 작성할 수 없습니다.");
        }

        if (durationMinutes != null && (durationMinutes < 10 || durationMinutes > 180)) {
            throw new IllegalArgumentException("상담 시간은 10분~180분 사이로 입력해주세요.");
        }

        if (summary != null && summary.length() > 2000) {
            throw new IllegalArgumentException("상담 요약은 2000자 이내로 작성해주세요.");
        }

        if (counselorNotes != null && counselorNotes.length() > 2000) {
            throw new IllegalArgumentException("상담사 메모는 2000자 이내로 작성해주세요.");
        }
    }
}
