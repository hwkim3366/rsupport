package com.notice.exception;

public class NotFoundNoticeException extends RuntimeException {

    private static String MESSAGE = "notice does not exist";

    public NotFoundNoticeException() {
        super(MESSAGE);
    }
}
