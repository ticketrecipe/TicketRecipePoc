package com.ticketrecipe.getcertify.registry;

import lombok.Builder;

import java.util.List;

public record TicketRegistryResponse (
        List<TicketQRCode> getCertifyQrCodes
) {
    @Builder
    public record TicketQRCode (
            String originalBarcodeId,
            String referenceId,
            String getCertifyQrCodeImage
    ) {}
}