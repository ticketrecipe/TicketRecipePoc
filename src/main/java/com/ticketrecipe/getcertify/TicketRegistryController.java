package com.ticketrecipe.getcertify;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/certify") //i.e. api.ticketrecipe.com/certify/tickets
public class TicketRegistryController {

    @Autowired
    private TicketRegistryService ticketRegistryService;

    @PostMapping("/tickets")
    public ResponseEntity<TicketRegistryResponse> certifyTickets(
            @RequestBody TicketCertifyRequest request) {

        TicketRegistryResponse response = ticketRegistryService.certifyTickets(request);
        return ResponseEntity.ok(response);
    }

//    @PutMapping("/tickets/{referenceId}/transfer")
//    public ResponseEntity<TicketRegistryResponse.TicketQRCode> transferOwnership(
//            @PathVariable String referenceId,
//            @RequestBody TicketTransferRequest transferRequest) {
//        log.info("Processing ticket transfer for Reference ID: {}", referenceId);
//        TicketRegistryResponse.TicketQRCode updatedQRCode = ticketRegistryService.transferTicketOwnership(referenceId, transferRequest);
//        return ResponseEntity.ok(updatedQRCode);
//    }
}
