package com.scms.app.model;

/**
 * 포트폴리오 공개 범위
 */
public enum PortfolioVisibility {
    PRIVATE("비공개"),
    PUBLIC("공개");

    private final String description;

    PortfolioVisibility(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
