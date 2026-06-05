package com.example.team3trimcommercepaymentproject.domain.refund.repository;

import com.example.team3trimcommercepaymentproject.domain.refund.entity.RefundItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefundItemRepository extends JpaRepository<RefundItem, Long> {

	@Query("SELECT COALESCE(SUM(ri.refundedAmount), 0) FROM RefundItem ri " +
		"JOIN ri.refund r WHERE r.payment.id = :paymentId AND r.status = 'COMPLETED'")
	long sumRefundedAmountByPaymentId(@Param("paymentId") Long paymentId);
}