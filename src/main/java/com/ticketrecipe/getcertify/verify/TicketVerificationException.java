package com.ticketrecipe.getcertify.verify;

import org.springframework.http.HttpStatus;

public class TicketVerificationException extends RuntimeException {

    private final String errorCode;

    public TicketVerificationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST; // 400 Bad Request
    }
}
