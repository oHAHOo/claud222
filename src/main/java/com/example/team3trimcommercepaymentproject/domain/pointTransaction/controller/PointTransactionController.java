package com.example.team3trimcommercepaymentproject.domain.pointTransaction.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.team3trimcommercepaymentproject.domain.pointTransaction.dto.GetPointTransactionResponse;
import com.example.team3trimcommercepaymentproject.domain.pointTransaction.service.PointTransactionService;
import com.example.team3trimcommercepaymentproject.global.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class PointTransactionController {
	private final PointTransactionService pointTransactionService;

	@GetMapping("/member/point-transaction")
	public ResponseEntity<ApiResponse<List<GetPointTransactionResponse>>> getPointTransaction(
		@AuthenticationPrincipal Long memberId) {
		return ResponseEntity.ok(ApiResponse.ok(pointTransactionService.getPointTransaction(memberId)));
	}

}
