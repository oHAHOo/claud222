package com.example.team3trimcommercepaymentproject.domain.order.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record PartialRefundRequest(
	@NotEmpty @Valid List<PartialRefundItemRequest> items,
	@NotBlank String reason
) {}
