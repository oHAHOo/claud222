package com.example.team3trimcommercepaymentproject.domain.member.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.team3trimcommercepaymentproject.domain.member.dto.MyPointResponse;
import com.example.team3trimcommercepaymentproject.domain.member.service.MemberService;
import com.example.team3trimcommercepaymentproject.global.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {

	private final MemberService memberService;

	@GetMapping("/me/point")
	public ResponseEntity<ApiResponse<MyPointResponse>> getMyPoint(
		@AuthenticationPrincipal Long memberId) {
		return ResponseEntity.ok(ApiResponse.ok(memberService.getMyPoint(memberId)));
	}
}
