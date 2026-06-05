package com.example.team3trimcommercepaymentproject.domain.payment.dto.request;

public record PaymentConfirmRequest(
        Long orderId,
        String portonePaymentId
) {
}
