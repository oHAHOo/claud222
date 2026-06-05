package com.example.team3trimcommercepaymentproject.domain.order.dto.response;

import com.example.team3trimcommercepaymentproject.domain.orderItem.dto.response.OrderPreviewItemResponse;

import java.util.List;

public record OrderPreviewResponse(
        List<OrderPreviewItemResponse> items,
        Long totalAmount
) {
}
