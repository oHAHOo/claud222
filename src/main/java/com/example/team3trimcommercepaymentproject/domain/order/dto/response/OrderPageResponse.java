package com.example.team3trimcommercepaymentproject.domain.order.dto.response;

import java.util.List;

public record OrderPageResponse(
        List<OrderSummaryResponse> orders,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
