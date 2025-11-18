package com.scms.app.model;

/**
 * 사용자 유형 Enum
 * - 내부 회원(재학생, 상담사, 관리자)과 외부 회원 구분
 */
public enum UserType {
    /**
     * 내부 회원 (users 테이블)
     * - 재학생, 상담사, 관리자
     */
    INTERNAL("내부회원"),

    /**
     * 외부 회원 (external_users 테이블)
     */
    EXTERNAL("외부회원");

    private final String description;

    UserType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
