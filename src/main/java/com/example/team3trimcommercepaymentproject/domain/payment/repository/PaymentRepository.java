package com.example.team3trimcommercepaymentproject.domain.payment.repository;

import com.example.team3trimcommercepaymentproject.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPortonePaymentId(String portonePaymentId);
}
