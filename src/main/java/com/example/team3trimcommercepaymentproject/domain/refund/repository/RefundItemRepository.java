package com.example.team3trimcommercepaymentproject.domain.refund.repository;

import com.example.team3trimcommercepaymentproject.domain.refund.entity.Refund;
import com.example.team3trimcommercepaymentproject.domain.refund.entity.RefundItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefundItemRepository extends JpaRepository<RefundItem, Long> {

	@Query("SELECT COALESCE(SUM(ri.refundedAmount), 0L) FROM RefundItem ri WHERE ri.refund.payment.id = :paymentId AND ri.refund.status = :status")
	Long sumRefundedAmountByPaymentIdAndStatus(@Param("paymentId") Long paymentId, @Param("status") Refund.RefundStatus status);
}
