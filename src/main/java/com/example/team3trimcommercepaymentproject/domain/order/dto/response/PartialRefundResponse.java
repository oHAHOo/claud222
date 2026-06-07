package com.example.team3trimcommercepaymentproject.domain.order.dto.response;

import com.example.team3trimcommercepaymentproject.domain.order.entity.OrderStatus;
import com.example.team3trimcommercepaymentproject.domain.payment.entity.PaymentStatus;

public record PartialRefundResponse(
	Long orderId,
	String orderNumber,
	OrderStatus orderStatus,
	PaymentStatus paymentStatus,
	Long totalRefundAmount,
	Long pointRefundAmount,
	Long pgRefundAmount
) {}
