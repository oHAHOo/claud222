package com.example.team3trimcommercepaymentproject.domain.cart.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.team3trimcommercepaymentproject.domain.cart.entity.Cart;

public interface CartRepository extends JpaRepository<Cart, Long> {

	Optional<Cart> findByMemberId(Long memberId);
}
