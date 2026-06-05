package com.example.team3trimcommercepaymentproject.domain.payment.dto.response;

import com.example.team3trimcommercepaymentproject.domain.payment.entity.PaymentStatus;

public record PaymentCreateResponse(
        Long paymentId,
        String portonePaymentId,
        PaymentStatus paymentStatus,
        Long totalAmount,
        Long usedPoint,
        Long pgAmount,
        Long earnedPoint
) {
}
