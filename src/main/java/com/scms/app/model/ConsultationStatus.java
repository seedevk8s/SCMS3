package com.scms.app.model;

/**
 * 상담 세션 상태
 */
public enum ConsultationStatus {
    PENDING("대기"),           // 신청 대기
    APPROVED("승인"),          // 신청 승인
    REJECTED("거부"),          // 신청 거부
    COMPLETED("완료"),         // 상담 완료
    CANCELLED("취소");         // 상담 취소

    private final String description;

    ConsultationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
