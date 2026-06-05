package com.example.team3trimcommercepaymentproject.domain.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.team3trimcommercepaymentproject.domain.auth.dto.LoginRequest;
import com.example.team3trimcommercepaymentproject.domain.auth.dto.LoginResponse;
import com.example.team3trimcommercepaymentproject.domain.auth.dto.SignupRequest;
import com.example.team3trimcommercepaymentproject.domain.auth.dto.SignupResponse;
import com.example.team3trimcommercepaymentproject.domain.auth.service.AuthService;
import com.example.team3trimcommercepaymentproject.global.response.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@PostMapping("/signup")
	public ResponseEntity<ApiResponse<SignupResponse>> signup(@Valid @RequestBody SignupRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(authService.signup(request)));
	}

	@PostMapping("/login")
	public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
		return ResponseEntity.ok(ApiResponse.ok(authService.login(request)));
	}

}
