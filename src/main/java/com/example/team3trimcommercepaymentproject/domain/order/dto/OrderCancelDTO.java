package com.example.team3trimcommercepaymentproject.domain.order.dto;

import com.example.team3trimcommercepaymentproject.domain.order.dto.response.OrderCancelResponse;

public record OrderCancelDTO(
	OrderCancelResponse response,
	String portonePaymentId,
	String cancelReason,
	boolean needsPgCancel,
	Long paymentId
) {}