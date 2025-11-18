package com.scms.app.model;

/**
 * 포트폴리오 항목 유형
 */
public enum PortfolioItemType {
    PROJECT("프로젝트"),
    ACHIEVEMENT("성과/수상"),
    CERTIFICATION("자격증"),
    ACTIVITY("활동"),
    COURSE("교육/강좌");

    private final String description;

    PortfolioItemType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
