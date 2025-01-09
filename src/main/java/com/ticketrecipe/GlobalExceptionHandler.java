package com.ticketrecipe;

import com.ticketrecipe.getcertify.GetCertifyException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GetCertifyException.class)
    public ResponseEntity<Map<String, Object>> GetCertifyException(GetCertifyException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("errorCode", ex.getErrorCode());
        response.put("message", ex.getMessage());
        return ResponseEntity.status(ex.getHttpStatus()).body(response);
    }
}
