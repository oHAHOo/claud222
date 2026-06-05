package com.example.team3trimcommercepaymentproject.domain.cart.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.team3trimcommercepaymentproject.domain.cart.entity.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

	// 회원 고유 번호와 상품 고유 번호로 기존 장바구니 상품 조회 (중복 상품 담기 판별용)
	Optional<CartItem> findByMemberIdAndProductId(Long memberId, Long productId);

	Optional<CartItem> findByMemberIdAndId(Long memberId, Long id);

	List<CartItem> findAllByMemberId(Long memberId);

	// INSERT, UPDATE, DELETE 쿼리에 필수
	// clearAutomatically = true 설정
	// Hibernate 1차 캐시(영속성 컨텍스트) 비워주기 (같은 트랜잭션 안에서 삭제 후 조회 시 캐시 데이터가 반환될 수 있음)
	@Modifying(clearAutomatically = true)
	// 벌크 삭제 쿼리로 한번의 DELETE로 모든 장바구니 상품 삭제
	@Query("DELETE FROM CartItem c WHERE c.cart.id = :cartId")
	void deleteAllByCartId(@Param("cartId") Long cartId);
}
