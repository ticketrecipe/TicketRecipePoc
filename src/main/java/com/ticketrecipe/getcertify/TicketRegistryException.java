package com.ticketrecipe.getcertify;

import org.springframework.http.HttpStatus;

public class TicketRegistryException extends RuntimeException {

    private final String errorCode;

    public TicketRegistryException(String message, String errorCode) {
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