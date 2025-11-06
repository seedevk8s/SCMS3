package com.scms.app.exception;

/**
 * 사용자를 찾을 수 없을 때 발생하는 예외
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(Integer studentNum) {
        super("사용자를 찾을 수 없습니다: 학번 " + studentNum);
    }
}
