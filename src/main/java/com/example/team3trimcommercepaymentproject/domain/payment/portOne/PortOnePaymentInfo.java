package com.example.team3trimcommercepaymentproject.domain.payment.portOne;

public record PortOnePaymentInfo(
        String paymentId,
        String staus,
        Long paidAmount
) {
    public boolean isPaid() {
        return "PAID".equals(staus);
    }

    public boolean isFailed() {
        return"FAILED".equals(staus)
                || "CANCELLED".equals(staus)
                || "CANCELED".equals(staus);
    }

}
