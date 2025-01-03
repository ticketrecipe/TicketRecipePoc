package com.ticketrecipe.getcertify.verify;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/certify/ticket") //i.e. api.ticketrecipe.com/certify/ticket/verification
@RequiredArgsConstructor
public class TicketVerificationController {

    private final TicketVerificationService ticketRegistryService;

    @PostMapping("/verification")
    public ResponseEntity<TicketVerificationResult> validateTicket(
            @RequestBody TicketVerificationRequest request) {
        TicketVerificationResult response = ticketRegistryService.validateTicket(request.getQrCodeData());
        return ResponseEntity.ok(response);
    }
}
