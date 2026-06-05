package com.example.team3trimcommercepaymentproject.domain.order.dto.request;

import java.util.List;

public record OrderPreviewRequest(
        List<Long> cartItemIds
) {
}