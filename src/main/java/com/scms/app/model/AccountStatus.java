package com.scms.app.model;

/**
 * 계정 상태 Enum
 */
public enum AccountStatus {
    /**
     * 활성
     */
    ACTIVE("활성"),

    /**
     * 비활성
     */
    INACTIVE("비활성"),

    /**
     * 정지
     */
    SUSPENDED("정지");

    private final String description;

    AccountStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
