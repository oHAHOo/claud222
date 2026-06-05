package com.example.team3trimcommercepaymentproject.domain.order.dto.response;

import com.example.team3trimcommercepaymentproject.domain.order.entity.OrderStatus;

import java.time.LocalDateTime;

public record OrderSummaryResponse(
        Long orderId,
        String orderNumber,
        OrderStatus orderStatus,
        Long totalAmount,
        LocalDateTime createdAt
) {
}