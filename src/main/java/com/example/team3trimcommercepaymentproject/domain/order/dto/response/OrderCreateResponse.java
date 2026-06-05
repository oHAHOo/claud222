package com.example.team3trimcommercepaymentproject.domain.order.dto.response;

import com.example.team3trimcommercepaymentproject.domain.order.entity.OrderStatus;
import com.example.team3trimcommercepaymentproject.domain.payment.dto.response.PaymentCreateResponse;

public record OrderCreateResponse(
        Long orderId,
        String orderNumber,
        OrderStatus orderStatus,
        PaymentCreateResponse payment
) {
}
