package com.scms.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * 상담사 일정 등록/수정 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CounselorScheduleRequest {

    private Integer dayOfWeek; // 1=월요일, 7=일요일
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean isAvailable;

    /**
     * 유효성 검증
     */
    public void validate() {
        if (dayOfWeek == null || dayOfWeek < 1 || dayOfWeek > 7) {
            throw new IllegalArgumentException("요일은 1(월요일)~7(일요일) 사이의 값이어야 합니다.");
        }

        if (startTime == null) {
            throw new IllegalArgumentException("시작 시간을 입력해주세요.");
        }

        if (endTime == null) {
            throw new IllegalArgumentException("종료 시간을 입력해주세요.");
        }

        if (startTime.isAfter(endTime) || startTime.equals(endTime)) {
            throw new IllegalArgumentException("시작 시간은 종료 시간보다 이전이어야 합니다.");
        }

        if (isAvailable == null) {
            isAvailable = true; // 기본값
        }
    }
}
