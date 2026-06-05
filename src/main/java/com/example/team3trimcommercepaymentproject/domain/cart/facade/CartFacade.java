package com.example.team3trimcommercepaymentproject.domain.cart.facade;

import org.springframework.stereotype.Component;

import com.example.team3trimcommercepaymentproject.domain.cart.dto.CartItemAddRequest;
import com.example.team3trimcommercepaymentproject.domain.cart.dto.CartItemAddResponse;
import com.example.team3trimcommercepaymentproject.domain.cart.service.CartService;
import com.example.team3trimcommercepaymentproject.domain.member.entity.Member;
import com.example.team3trimcommercepaymentproject.domain.member.service.MemberService;
import com.example.team3trimcommercepaymentproject.domain.product.entity.Product;
import com.example.team3trimcommercepaymentproject.domain.product.service.ProductService;
import com.example.team3trimcommercepaymentproject.global.exception.BusinessException;
import com.example.team3trimcommercepaymentproject.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CartFacade {

	private final CartService cartService;
	private final ProductService productService;
	private final MemberService memberService;

	// 다른 서비스를 이용하는 Cart의 로직을 구현하기 위해서 Facade에서 구현
	public CartItemAddResponse addItem(Long memberId, CartItemAddRequest request) {
		// 상품 조회 트랜잭션
		Product product = productService.findProductEntity(request.productId());
		if (product.getSaleStatus() == Product.SaleStatus.SOLD_OUT) {
			throw new BusinessException(ErrorCode.OUT_OF_STOCK);
		}

		// 회원 조회 트랜잭션
		Member member = memberService.findMemberEntity(memberId);

		// 장바구니 생성(있을 시 조회만), 장바구니에 상품 추가(저장/쓰기)
		Long cartItemId = cartService.getOrCreateCartAndAddItem(member, product, request.quantity());

		return new CartItemAddResponse(cartItemId);
	}

}
