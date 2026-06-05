package com.example.team3trimcommercepaymentproject.domain.member.dto;

import java.time.LocalDateTime;

public record MyPointResponse(
	Long memberId,
	String email,
	Long point,
	LocalDateTime modifiedAt
) {
}
