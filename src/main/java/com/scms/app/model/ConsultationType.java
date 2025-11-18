package com.scms.app.model;

/**
 * 상담 유형
 */
public enum ConsultationType {
    CAREER("진로상담"),          // 진로 및 취업 상담
    ACADEMIC("학업상담"),        // 학업 및 성적 상담
    PSYCHOLOGICAL("심리상담"),   // 심리 및 정서 상담
    RELATIONSHIP("대인관계"),     // 대인관계 상담
    OTHER("기타");               // 기타 상담

    private final String description;

    ConsultationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
