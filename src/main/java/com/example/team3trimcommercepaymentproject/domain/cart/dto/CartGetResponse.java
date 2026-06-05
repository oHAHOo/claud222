package com.example.team3trimcommercepaymentproject.domain.cart.dto;

import java.util.List;

public record CartGetResponse(
	List<CartItemResponse> items,
	Integer totalAmount
) {}