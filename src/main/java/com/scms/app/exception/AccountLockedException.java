package com.scms.app.exception;

/**
 * 계정이 잠겨있을 때 발생하는 예외
 */
public class AccountLockedException extends RuntimeException {

    public AccountLockedException() {
        super("계정이 잠겨있습니다. 관리자에게 문의하세요");
    }

    public AccountLockedException(String message) {
        super(message);
    }
}
