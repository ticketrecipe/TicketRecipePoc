package com.ticketrecipe.getcertify;

import com.ticketrecipe.getcertify.registry.GetCertifyRegistryService;
import com.ticketrecipe.getcertify.registry.TicketCertifyRequest;
import com.ticketrecipe.getcertify.registry.TicketRegistryResponse;
import com.ticketrecipe.getcertify.validate.GetCertifyAccessService;
import com.ticketrecipe.getcertify.validate.SecuredAccessCode;
import com.ticketrecipe.getcertify.verify.TicketVerificationResult;
import com.ticketrecipe.getcertify.verify.GetCertifyVerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1") //i.e. api.ticketrecipe.com/getcertify/tickets
public class GetCertifyController {

    @Autowired
    private GetCertifyRegistryService ticketRegistryService;
    @Autowired
    private GetCertifyAccessService getCertifyAccessService;
    @Autowired
    private GetCertifyVerificationService ticketVerificationService;

    @PostMapping("/certify/tickets")
    public ResponseEntity<TicketRegistryResponse> certifyTickets(@RequestBody TicketCertifyRequest request) {
        TicketRegistryResponse response = ticketRegistryService.certifyTickets(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/getcertify/ticket/verify")
    public ResponseEntity<TicketVerificationResult> verifyTicket(@RequestBody  GetCertifyQRCode request) {
        TicketVerificationResult response = ticketVerificationService.verify(request.getCertifyQrCode());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/getcertify/ticket/validate")
    public ResponseEntity<SecuredAccessCode> validateTicket(@RequestBody GetCertifyQRCode request) {
        try {
            SecuredAccessCode response = getCertifyAccessService.getSecuredAccessCode(request.getCertifyQrCode());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
