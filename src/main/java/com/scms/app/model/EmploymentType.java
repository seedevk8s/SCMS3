package com.scms.app.model;

/**
 * 외부취업 활동 유형
 */
public enum EmploymentType {
    INTERNSHIP("인턴십", "기업 인턴십 프로그램"),
    FIELD_TRAINING("현장실습", "산학협력 현장실습"),
    PROJECT("외부 프로젝트", "외부 기업/기관 프로젝트 참여"),
    JOB("취업", "정규직/계약직 취업"),
    STARTUP("창업", "개인 창업 또는 스타트업");

    private final String displayName;
    private final String description;

    EmploymentType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
