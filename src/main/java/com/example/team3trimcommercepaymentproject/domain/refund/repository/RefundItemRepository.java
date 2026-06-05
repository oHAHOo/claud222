package com.example.team3trimcommercepaymentproject.domain.refund.repository;

import com.example.team3trimcommercepaymentproject.domain.refund.entity.RefundItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundItemRepository extends JpaRepository<RefundItem, Long> {
}