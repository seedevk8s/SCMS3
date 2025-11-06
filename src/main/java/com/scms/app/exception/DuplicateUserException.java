package com.scms.app.exception;

/**
 * 중복된 사용자가 있을 때 발생하는 예외
 */
public class DuplicateUserException extends RuntimeException {

    public DuplicateUserException(String message) {
        super(message);
    }

    public DuplicateUserException(String field, String value) {
        super(String.format("이미 존재하는 %s입니다: %s", field, value));
    }
}
