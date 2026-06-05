package com.example.team3trimcommercepaymentproject.domain.order.controller;

import com.example.team3trimcommercepaymentproject.domain.order.dto.request.OrderCancelRequest;
import com.example.team3trimcommercepaymentproject.domain.order.dto.request.OrderCreateRequest;
import com.example.team3trimcommercepaymentproject.domain.order.dto.request.OrderPreviewRequest;
import com.example.team3trimcommercepaymentproject.domain.order.dto.response.*;
import com.example.team3trimcommercepaymentproject.domain.order.facade.OrderCancelFacade;
import com.example.team3trimcommercepaymentproject.domain.order.service.OrderService;
import com.example.team3trimcommercepaymentproject.global.jwt.JwtProvider;
import com.example.team3trimcommercepaymentproject.global.response.ApiResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

	private final OrderService orderService;
	private final OrderCancelFacade orderCancelFacade;

	/**
	 * 주문서 미리보기
	 */
	@PostMapping("/preview")
	public ResponseEntity<ApiResponse<OrderPreviewResponse>> preview(
		@AuthenticationPrincipal Long memberId,
		@RequestBody OrderPreviewRequest request
	) {
		OrderPreviewResponse response = orderService.preview(memberId, request);
		return ResponseEntity.ok(ApiResponse.ok(response));
	}

	/**
	 * 주문/결제 동시 생성
	 */
	@PostMapping
	public ResponseEntity<ApiResponse<OrderCreateResponse>> create(
		@AuthenticationPrincipal Long memberId,
		@RequestBody OrderCreateRequest request
	) {
		OrderCreateResponse response = orderService.createOrderWithPayment(memberId, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
	}

	/**
	 * 내 주문 내역 조회
	 */
	@GetMapping
	public ResponseEntity<ApiResponse<OrderPageResponse>> findOrders(
		@AuthenticationPrincipal Long memberId,
		@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
		Pageable pageable
	) {
		OrderPageResponse response = orderService.findOrders(memberId, pageable);
		return ResponseEntity.ok(ApiResponse.ok(response));
	}

	/**
	 * 주문 상세 조회
	 */
	@GetMapping("/{orderId}")
	public ResponseEntity<ApiResponse<OrderDetailResponse>> findOrderDetail(
		@AuthenticationPrincipal Long memberId,
		@PathVariable Long orderId
	) {
		OrderDetailResponse response = orderService.findByIdOrder(memberId, orderId);
		return ResponseEntity.ok(ApiResponse.ok(response));
	}

	/**
	 * 주문 취소 (PAID 상태면 환불 이력 저장 + PG 취소 포함)
	 */
	@PostMapping("/{orderId}/cancel")
	public ResponseEntity<ApiResponse<OrderCancelResponse>> cancel(
		@AuthenticationPrincipal Long memberId,
		@PathVariable Long orderId,
		@RequestBody OrderCancelRequest request
	) {
		OrderCancelResponse response = orderCancelFacade.cancel(memberId, orderId, request);
		return ResponseEntity.ok(ApiResponse.ok(response));
	}
}