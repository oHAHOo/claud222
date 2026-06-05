package com.example.team3trimcommercepaymentproject.domain.cart.dto;

import com.example.team3trimcommercepaymentproject.domain.cart.entity.CartItem;

public record CartItemResponse(
	Long id,
	Long productId,
	String productName,
	Integer price,
	Integer quantity,
	Integer subtotal
) {
	public static CartItemResponse from(CartItem cartItem) {
		return new CartItemResponse(
			cartItem.getId(),
			cartItem.getProduct().getId(),
			cartItem.getProduct().getName(),
			cartItem.getProduct().getPrice(),
			cartItem.getQuantity(),
			cartItem.getProduct().getPrice() * cartItem.getQuantity()
		);
	}
}
