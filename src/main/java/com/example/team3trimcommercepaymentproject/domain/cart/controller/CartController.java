package com.example.team3trimcommercepaymentproject.domain.cart.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.team3trimcommercepaymentproject.domain.cart.dto.CartGetResponse;
import com.example.team3trimcommercepaymentproject.domain.cart.dto.CartItemAddRequest;
import com.example.team3trimcommercepaymentproject.domain.cart.dto.CartItemAddResponse;
import com.example.team3trimcommercepaymentproject.domain.cart.dto.CartItemResponse;
import com.example.team3trimcommercepaymentproject.domain.cart.dto.CartUpdateRequest;
import com.example.team3trimcommercepaymentproject.domain.cart.facade.CartFacade;
import com.example.team3trimcommercepaymentproject.domain.cart.service.CartService;
import com.example.team3trimcommercepaymentproject.global.response.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/cart/items")
public class CartController {

	private final CartService cartService;
	private final CartFacade cartFacade;

	// 장바구니 상품 추가
	@PostMapping
	public ResponseEntity<ApiResponse<CartItemAddResponse>> addItem(
		@AuthenticationPrincipal Long memberId,
		@Valid @RequestBody CartItemAddRequest request
	) {
		CartItemAddResponse response = cartFacade.addItem(memberId, request);

		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
	}

	// 장바구니 상품 전체 조회
	@GetMapping
	public ResponseEntity<ApiResponse<CartGetResponse>> getAllCartItem(@AuthenticationPrincipal Long memberId) {
		CartGetResponse response = cartService.getAllCartItem(memberId);

		return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(response));
	}

	// 장바구니 상품 수량 수정
	@PatchMapping("/{cartItemId}")
	public ResponseEntity<ApiResponse<CartItemResponse>> updateItem(
		@AuthenticationPrincipal Long memberId,
		@PathVariable Long cartItemId,
		@Valid @RequestBody CartUpdateRequest request
	) {
		CartItemResponse response = cartService.updateQuantity(memberId, cartItemId, request);

		return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(response));
	}

	// 장바구니 상품 개별 삭제
	@DeleteMapping("/{cartItemId}")
	public ResponseEntity<ApiResponse<Void>> deleteOneItem(
		@AuthenticationPrincipal Long memberId,
		@PathVariable Long cartItemId
	) {
		cartService.deleteOneItem(memberId, cartItemId);

		return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok());
	}

	// 장바구니 비우기
	@DeleteMapping
	public ResponseEntity<ApiResponse<Void>> clearCart(@AuthenticationPrincipal Long memberId) {
		cartService.clearCart(memberId);
		return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok());
	}

}
