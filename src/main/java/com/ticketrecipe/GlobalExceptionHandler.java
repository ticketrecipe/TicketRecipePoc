package com.ticketrecipe;

import com.ticketrecipe.getcertify.TicketRegistryException;
import com.ticketrecipe.getcertify.verify.TicketVerificationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TicketVerificationException.class)
    public ResponseEntity<Map<String, Object>> handleTicketNotFoundException(TicketVerificationException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("errorCode", ex.getErrorCode());
        response.put("message", ex.getMessage());
        return ResponseEntity.status(ex.getHttpStatus()).body(response);
    }

    @ExceptionHandler(TicketRegistryException.class)
    public ResponseEntity<Map<String, Object>> ticketRegistryException(TicketRegistryException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("errorCode", ex.getErrorCode());
        response.put("message", ex.getMessage());
        return ResponseEntity.status(ex.getHttpStatus()).body(response);
    }
}
