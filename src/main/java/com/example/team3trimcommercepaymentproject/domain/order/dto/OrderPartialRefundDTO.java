package com.example.team3trimcommercepaymentproject.domain.order.dto;

import com.example.team3trimcommercepaymentproject.domain.order.dto.response.PartialRefundResponse;

public record OrderPartialRefundDTO(
	Long refundId,
	String portonePaymentId,
	Long pgRefundAmount,
	String reason,
	PartialRefundResponse response
) {}
