package com.scms.app.model;

/**
 * 프로그램 상태
 */
public enum ProgramStatus {
    SCHEDULED("접수예정"),
    OPEN("접수중"),
    FULL("접수완료"),
    CLOSED("마감");

    private final String description;

    ProgramStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
