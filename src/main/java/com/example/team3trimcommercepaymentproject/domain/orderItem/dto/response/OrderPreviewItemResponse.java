package com.example.team3trimcommercepaymentproject.domain.orderItem.dto.response;

public record OrderPreviewItemResponse(
        Long cartItemId,
        Long productId,
        String productName,
        Long price,
        Integer quantity,
        Long subtotalAmount
) {
}