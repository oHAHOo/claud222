package com.example.team3trimcommercepaymentproject.domain.order.repository;

import com.example.team3trimcommercepaymentproject.domain.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("""
    SELECT o
    FROM Order o
    WHERE o.member.id = :memberId
    ORDER BY o.createdAt DESC
""")
    Page<Order> findOrderPageByMemberId(
            @Param("memberId") Long memberId,
            Pageable pageable
    );

    @Query("""
    SELECT o
    FROM Order o
    JOIN FETCH o.payment
    JOIN FETCH o.orderItems oi
    JOIN FETCH oi.product
    WHERE o.id = :orderId
    AND o.member.id = :memberId
""")
    Optional<Order> findOrderDetailByIdAndMemberId(
            @Param("orderId") Long orderId,
            @Param("memberId") Long memberId
    );
}
