package com.ticketrecipe.getcertify;

import org.springframework.http.HttpStatus;

public class GetCertifyException extends RuntimeException {

    private final String errorCode;

    public GetCertifyException(String message, String errorCode) {
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
