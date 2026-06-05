package com.example.team3trimcommercepaymentproject.global.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	// Common
	INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON_001", "입력값이 올바르지 않습니다."),
	INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_002", "서버 내부 오류가 발생했습니다."),

	// Member
	MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_001", "회원을 찾을 수 없습니다."),
	DUPLICATE_EMAIL(HttpStatus.CONFLICT, "MEMBER_002", "이미 존재하는 이메일입니다."),
	INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "MEMBER_003", "이메일 또는 비밀번호가 올바르지 않습니다."),

	// Auth
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_001", "인증이 필요합니다."),
	INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_002", "유효하지 않은 토큰입니다."),
	// Product
	PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT_001", "상품을 찾을 수 없습니다."),
	PRODUCT_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "PRODUCT_002", "판매 가능한 상품이 아닙니다."),
	OUT_OF_STOCK(HttpStatus.BAD_REQUEST, "PRODUCT_003", "상품 재고가 부족합니다."),

	// Point
	POINT_NOT_ENOUGH(HttpStatus.BAD_REQUEST, "POINT_001", "포인트 잔액이 부족합니다."),
	POINT_EXCEEDS_ORDER_AMOUNT(HttpStatus.BAD_REQUEST, "POINT_002", "사용 포인트는 주문 금액을 초과할 수 없습니다."),

	// Order
	ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER_001", "주문을 찾을 수 없습니다."),
	ORDER_NOT_CANCELABLE(HttpStatus.BAD_REQUEST, "ORDER_002", "취소할 수 없는 주문 상태입니다."),

	// Payment
	PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "PAYMENT_001", "결제 정보를 찾을 수 없습니다."),
	PAYMENT_ALREADY_PROCESSED(HttpStatus.CONFLICT, "PAYMENT_002", "이미 처리된 결제입니다."),
	PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "PAYMENT_003", "결제 금액이 일치하지 않습니다."),

	//Cart
	CART_EMPTY(HttpStatus.BAD_REQUEST, "CART_001", "장바구니가 비어있습니다."),
	CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "CART_002", "장바구니에 존재하지 않는 상품입니다."),
	INVALID_QUANTITY(HttpStatus.BAD_REQUEST, "CART_003", "상품 수량은 1개 이상이어야 합니다."),
	CART_NOT_FOUND(HttpStatus.NOT_FOUND, "CART_004", "장바구니가 존재하지 않습니다."),

	// Refund
	ORDER_NOT_REFUNDABLE(HttpStatus.BAD_REQUEST, "REFUND_001", "환불할 수 없는 결제 상태입니다."),
	ORDER_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "REFUND_002", "주문 상품을 찾을 수 없습니다."),
	REFUND_QUANTITY_EXCEEDED(HttpStatus.BAD_REQUEST, "REFUND_003", "환불 가능 수량을 초과했습니다."),
	REFUND_ITEMS_EMPTY(HttpStatus.BAD_REQUEST, "REFUND_004", "환불 대상 상품이 없습니다.");



	private final HttpStatus status;
	private final String code;
	private final String message;
}