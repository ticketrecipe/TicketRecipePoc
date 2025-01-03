package com.ticketrecipe.getcertify;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class TicketRegistryResponse {

    private List<TicketQRCode> qrCodes;

    @Data
    @AllArgsConstructor
    public static class TicketQRCode {
        private String originalBarcodeId;
        private String referenceId;
        private String qrCodeImage; // Base64 PNG image of the QR code
    }
}
