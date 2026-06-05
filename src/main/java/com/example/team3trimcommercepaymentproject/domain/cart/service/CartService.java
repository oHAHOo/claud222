package com.example.team3trimcommercepaymentproject.domain.cart.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.team3trimcommercepaymentproject.domain.cart.dto.CartGetResponse;
import com.example.team3trimcommercepaymentproject.domain.cart.dto.CartItemResponse;
import com.example.team3trimcommercepaymentproject.domain.cart.dto.CartUpdateRequest;
import com.example.team3trimcommercepaymentproject.domain.cart.entity.Cart;
import com.example.team3trimcommercepaymentproject.domain.cart.entity.CartItem;
import com.example.team3trimcommercepaymentproject.domain.cart.repository.CartItemRepository;
import com.example.team3trimcommercepaymentproject.domain.cart.repository.CartRepository;
import com.example.team3trimcommercepaymentproject.domain.member.entity.Member;
import com.example.team3trimcommercepaymentproject.domain.product.entity.Product;
import com.example.team3trimcommercepaymentproject.global.exception.BusinessException;
import com.example.team3trimcommercepaymentproject.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

	private final CartItemRepository cartItemRepository;
	private final CartRepository cartRepository;

	// 장바구니가 존재한다면 조회하고 없다면 생성
	public Cart getOrCreateCart(Member member) {
		return cartRepository.findByMemberId(member.getId())
			.orElseGet(() -> cartRepository.save(new Cart(member)));
	}

	@Transactional(readOnly = true)
	public CartItem getCartItemEntity(Long memberId, Long cartItemId) {
		return cartItemRepository.findByMemberIdAndId(memberId, cartItemId).orElseThrow(
			() -> new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND)
		);
	}

	@Transactional
	public Long getOrCreateCartAndAddItem(Member member, Product product, int quantity) {
		// 장바구니와 장바구니 상품 쓰기 트랜잭션
		// getOrCreateCart self-invocation 문제 있음 트랜잭션 무시 (해당 메서드의 트랜잭션을 제거해서 트랜잭션 진입점을 현재 호출하는
		// 메서드에서만으로 함)
		Cart cart = getOrCreateCart(member);
		CartItem cartItem = new CartItem(member, cart, product, quantity);
		// addItem self-invocation 문제 있음 트랜잭션 무시 (해당 메서드의 트랜잭션을 제거해서 트랜잭션 진입점을 현재 호출하는
		// 메서드에서만으로 함)
		return addItem(cartItem);
	}

	/**
	 * 1. 사용자가 상품 담기를 누름
	 * 2. 상품 정보 id, 사용자 id를 받음
	 * 3. 이미 장바구니에 담긴 상품인지 검사 있다면 갯수 증가 끝
	 * 4. 장바구니 아이템을 하나 만들어서 저장
	 * 트랜잭션 부재 절대로 **단독 호출 금지** dirty checking 미동작합니다.
	 * @param cartItem 장바구니 상품
	 * @return 담긴 상품 고유 번호
	 */
	public Long addItem(CartItem cartItem) {
		Optional<CartItem> existItem = cartItemRepository.findByMemberIdAndProductId(
			cartItem.getMember().getId(), cartItem.getProduct().getId());

		if (existItem.isPresent()) {
			CartItem foundItem = existItem.get();
			foundItem.addQuantity(cartItem.getQuantity());
			return foundItem.getId();
		} else {
			return cartItemRepository.save(cartItem).getId();
		}
	}

	// 장바구니에 있는 모든 상품 조회
	@Transactional(readOnly = true)
	public CartGetResponse getAllCartItem(Long memberId) {
		// 현재 장바구니에 상품이 존재하지 않을 경우 예외처리
		List<CartItem> foundItems = cartItemRepository.findAllByMemberId(memberId);
		if(foundItems.isEmpty())
			throw new BusinessException(ErrorCode.CART_EMPTY);

		// 장바구니 상품들을 데이터 전달용 dto로 변경
		List<CartItemResponse> items = foundItems.stream()
			.map(CartItemResponse::from)
			.toList();

		// 총 가격 계산
		int totalAmount = items.stream()
			.mapToInt(CartItemResponse::subtotal)
			.sum();

		// 응답용 dto에 담아서 반환
		return new CartGetResponse(items, totalAmount);
	}

	// 상품 수량 수정
	@Transactional
	public CartItemResponse updateQuantity(Long memberId, Long cartItemId, CartUpdateRequest request) {
		CartItem item = getCartItemEntity(memberId, cartItemId);

		item.updateQuantity(request.quantity());

		return CartItemResponse.from(item);
	}

	// 상품 개별 삭제
	@Transactional
	public void deleteOneItem(Long memberId, Long cartItemId) {
		CartItem item = getCartItemEntity(memberId, cartItemId);

		cartItemRepository.delete(item);
	}

	// 장바구니 비우기
	@Transactional
	public void clearCart(Long memberId) {
		Cart cart = cartRepository.findByMemberId(memberId).orElseThrow(
			() -> new BusinessException(ErrorCode.CART_EMPTY)
		);
		cartItemRepository.deleteAllByCartId(cart.getId());
	}
}
