package com.example.team3trimcommercepaymentproject.domain.order.dto.response;

import com.example.team3trimcommercepaymentproject.domain.order.entity.OrderStatus;
import com.example.team3trimcommercepaymentproject.domain.payment.entity.PaymentStatus;

import java.time.LocalDateTime;

public record OrderCancelResponse(
        Long orderId,
        String orderNumber,
        OrderStatus orderStatus,
        PaymentStatus paymentStatus,
        String cancelReason,
        LocalDateTime canceledAt
) {
}