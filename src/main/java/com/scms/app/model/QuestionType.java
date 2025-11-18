package com.scms.app.model;

/**
 * 설문 질문 유형
 */
public enum QuestionType {
    SINGLE_CHOICE("객관식 단일선택"),
    MULTIPLE_CHOICE("객관식 복수선택"),
    SHORT_TEXT("주관식 단답형"),
    LONG_TEXT("주관식 서술형"),
    SCALE("척도형");

    private final String displayName;

    QuestionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
