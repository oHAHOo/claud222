package com.example.team3trimcommercepaymentproject.domain.order.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PartialRefundRequest(
	@NotEmpty(message = "환불 대상 상품을 입력해 주세요.")
	@Valid
	List<RefundItemRequest> items,

	@NotBlank(message = "취소 사유를 입력해 주세요.")
	String cancelReason
) {
	public record RefundItemRequest(
		@NotNull(message = "주문 상품 ID를 입력해 주세요.")
		Long orderItemId,

		@NotNull(message = "환불 수량을 입력해 주세요.")
		@Min(value = 1, message = "환불 수량은 1 이상이어야 합니다.")
		Integer quantity
	) {}
}
