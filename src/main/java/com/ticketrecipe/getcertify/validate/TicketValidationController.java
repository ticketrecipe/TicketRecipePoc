package com.ticketrecipe.getcertify.validate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/v1/certify/ticket") //i.e. api.ticketrecipe.com/certify/ticket/validation
@Slf4j
public class TicketValidationController {

    private final TicketAccessControlService ticketAccessService;

    @PostMapping("/validation")
    public ResponseEntity<EmbeddedAccessCode> decryptAccessCode(@RequestBody TicketAccessControlRequest request) {
        try {
            EmbeddedAccessCode response = ticketAccessService.getEmbeddedBarCode(request.getQrCodeData());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
