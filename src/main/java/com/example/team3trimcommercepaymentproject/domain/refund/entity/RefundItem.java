package com.example.team3trimcommercepaymentproject.domain.refund.entity;

import com.example.team3trimcommercepaymentproject.domain.orderItem.entity.OrderItem;
import com.example.team3trimcommercepaymentproject.global.entity.BaseEntity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "refund_items")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefundItem extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "refund_id", nullable = false)
	private Refund refund;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_item_id", nullable = false)
	private OrderItem orderItem;

	@Column(name = "refunded_quantity", nullable = false)
	private Integer refundedQuantity;

	@Column(name = "refunded_amount", nullable = false)
	private Long refundedAmount;

	@Builder
	private RefundItem(Refund refund, OrderItem orderItem, int refundedQuantity, long refundedAmount) {
		this.refund = refund;
		this.orderItem = orderItem;
		this.refundedQuantity = refundedQuantity;
		this.refundedAmount = refundedAmount;
	}
}