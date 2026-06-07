package com.example.team3trimcommercepaymentproject.domain.refund.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PartialRefundItemRequest(
	@NotNull Long orderItemId,
	@Min(1) int quantity
) {}
