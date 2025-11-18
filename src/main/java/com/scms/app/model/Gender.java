package com.scms.app.model;

/**
 * 성별 Enum
 */
public enum Gender {
    /**
     * 남성
     */
    M("남성"),

    /**
     * 여성
     */
    F("여성"),

    /**
     * 기타
     */
    OTHER("기타");

    private final String description;

    Gender(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
