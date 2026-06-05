package com.example.team3trimcommercepaymentproject.domain.refund.entity;

import com.example.team3trimcommercepaymentproject.domain.payment.entity.Payment;
import com.example.team3trimcommercepaymentproject.global.entity.BaseEntity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "refunds")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Refund extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "payment_id", nullable = false)
	private Payment payment;

	@Column(nullable = false, length = 255)
	private String reason;

	@Column(name = "point_refund_price", nullable = false)
	private Long pointRefundPrice;

	@Column(name = "pg_refund_price", nullable = false)
	private Long pgRefundPrice;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private RefundStatus status;

	@Builder
	private Refund(
		Payment payment,
		String reason,
		Long pointRefundPrice,
		Long pgRefundPrice,
		RefundStatus status
	) {
		this.payment = payment;
		this.reason = reason;
		this.pointRefundPrice = pointRefundPrice;
		this.pgRefundPrice = pgRefundPrice;
		this.status = status == null ? RefundStatus.REQUESTED : status;
	}

	// 상태 변경
	public void complete() {
		this.status = RefundStatus.COMPLETED;
	}

	public void fail() {
		this.status = RefundStatus.FAILED;
	}

	public enum RefundStatus {
		REQUESTED,
		COMPLETED,
		FAILED
	}
}
