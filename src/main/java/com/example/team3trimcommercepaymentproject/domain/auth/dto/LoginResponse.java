package com.example.team3trimcommercepaymentproject.domain.auth.dto;

public record LoginResponse(
	String accessToken,
	String tokenType,
	int expiresIn,
	MemberInfo member
) {
	public record MemberInfo(
		Long id,
		String email,
		String name
	) {
	}
}