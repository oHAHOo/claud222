package com.example.team3trimcommercepaymentproject.domain.auth.dto;

import java.time.LocalDateTime;

public record SignupResponse(
	Long id,
	String email,
	String name,
	String phoneNumber,
	LocalDateTime createdAt
) {}