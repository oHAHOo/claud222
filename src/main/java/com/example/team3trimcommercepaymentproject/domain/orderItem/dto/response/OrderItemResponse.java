package com.example.team3trimcommercepaymentproject.domain.orderItem.dto.response;

public record OrderItemResponse(
        Long orderItemId,
        Long productId,
        String productName,
        Long price,
        Integer quantity,
        Integer refundedQuantity,
        Long subtotalAmount
) {
}
