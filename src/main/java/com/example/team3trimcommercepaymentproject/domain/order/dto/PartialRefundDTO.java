package com.example.team3trimcommercepaymentproject.domain.order.dto;

import com.example.team3trimcommercepaymentproject.domain.order.dto.response.PartialRefundResponse;

import java.util.List;

public record PartialRefundDTO(
	PartialRefundResponse response,
	String portonePaymentId,
	String cancelReason,
	long pgRefundAmount,
	Long paymentId,
	List<RefundItemData> items,
	long totalPointRefundAmount
) {
	public record RefundItemData(
		Long orderItemId,
		int quantity,
		long itemTotalAmount,
		long itemPointRefundAmount,
		long itemPgRefundAmount
	) {}
}
