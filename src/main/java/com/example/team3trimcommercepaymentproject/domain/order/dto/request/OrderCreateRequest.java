package com.example.team3trimcommercepaymentproject.domain.order.dto.request;

import java.util.List;

public record OrderCreateRequest(
        List<Long> cartItemIds,
        Long usedPoint
) {
}
