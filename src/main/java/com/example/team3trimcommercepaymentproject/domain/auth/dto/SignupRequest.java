package com.example.team3trimcommercepaymentproject.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SignupRequest(
	@NotBlank(message = "이메일을 입력하세요")
	@Email(message = "올바른 이메일 형식이 아닙니다")
	String email,

	@NotBlank(message = "비밀번호를 입력하세요")
	String password,

	@NotBlank(message = "이름을 입력하세요")
	String name,

	@NotBlank(message = "전화번호를 입력하세요")
	String phoneNumber
) {
}



