package com.example.team3trimcommercepaymentproject.domain.order.dto.response;

import com.example.team3trimcommercepaymentproject.domain.order.entity.OrderStatus;
import com.example.team3trimcommercepaymentproject.domain.orderItem.dto.response.OrderItemResponse;
import com.example.team3trimcommercepaymentproject.domain.payment.entity.PaymentStatus;

import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailResponse(
        Long orderId,
        String orderNumber,
        OrderStatus orderStatus,
        PaymentStatus paymentStatus,
        Long totalAmount,
        Long usedPoint,
        Long pgAmount,
        Long earnedPoint,
        List<OrderItemResponse> items,
        LocalDateTime createdAt
) {
}
