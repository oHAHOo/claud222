package com.example.team3trimcommercepaymentproject.domain.refund.repository;

import com.example.team3trimcommercepaymentproject.domain.refund.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundRepository extends JpaRepository<Refund, Long> {
}