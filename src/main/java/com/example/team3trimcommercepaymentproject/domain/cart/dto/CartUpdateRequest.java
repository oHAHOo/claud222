package com.example.team3trimcommercepaymentproject.domain.cart.dto;

import jakarta.validation.constraints.Min;

public record CartUpdateRequest(
	@Min(value = 1, message = "수량은 1 이상이어야 합니다")
	Integer quantity) {
}
