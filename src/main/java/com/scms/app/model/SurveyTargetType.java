package com.scms.app.model;

/**
 * 설문 대상 유형
 */
public enum SurveyTargetType {
    ALL("전체"),
    STUDENT("학생"),
    SPECIFIC("특정 대상");

    private final String displayName;

    SurveyTargetType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
