package com.scms.app.model;

/**
 * 알림 유형
 */
public enum NotificationType {
    APPLICATION_APPROVED("신청 승인", "프로그램 신청이 승인되었습니다."),
    APPLICATION_REJECTED("신청 거부", "프로그램 신청이 거부되었습니다."),
    APPLICATION_CANCELLED("신청 취소", "프로그램 신청이 취소되었습니다."),
    PROGRAM_STARTING("프로그램 시작", "프로그램이 곧 시작됩니다."),
    DEADLINE_APPROACHING("마감 임박", "프로그램 신청 마감이 임박했습니다.");

    private final String title;
    private final String description;

    NotificationType(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}
