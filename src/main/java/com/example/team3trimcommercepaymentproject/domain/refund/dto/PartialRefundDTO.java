package com.example.team3trimcommercepaymentproject.domain.refund.dto;

import com.example.team3trimcommercepaymentproject.domain.refund.dto.response.PartialRefundResponse;

public record PartialRefundDTO(
	Long refundId,
	String portonePaymentId,
	Long pgRefundAmount,
	String reason,
	PartialRefundResponse response
) {}
