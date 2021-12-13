package com.notice.exception;

public class NotFoundAttachmentException extends RuntimeException {

    private static String MESSAGE = "attachment does not exist";

    public NotFoundAttachmentException() {
        super(MESSAGE);
    }
}
