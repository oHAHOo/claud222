package com.example.team3trimcommercepaymentproject.domain.pointTransaction.entity;

import com.example.team3trimcommercepaymentproject.domain.member.entity.Member;
import com.example.team3trimcommercepaymentproject.domain.payment.entity.Payment;
import com.example.team3trimcommercepaymentproject.global.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "point_transactions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class PointTransaction extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private TransactionType transactionType;

	@Column(nullable = false)
	private Long amount;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "payment_id", nullable = false)
	private Payment payment;

	public PointTransaction(TransactionType transactionType, Long amount, Member member, Payment payment) {
		this.transactionType = transactionType;
		this.amount = amount;
		this.member = member;
		this.payment = payment;
	}

	public static PointTransaction earn(Member member, Payment payment, Long amount) {
		PointTransaction pointTransaction = new PointTransaction();
		pointTransaction.member = member;
		pointTransaction.payment = payment;
		pointTransaction.transactionType = TransactionType.EARN;
		pointTransaction.amount = amount;
		return pointTransaction;
	}

	public static PointTransaction use(Member member, Payment payment, Long amount) {
		PointTransaction pointTransaction = new PointTransaction();
		pointTransaction.member = member;
		pointTransaction.payment = payment;
		pointTransaction.transactionType = TransactionType.USE;
		pointTransaction.amount = amount;
		return pointTransaction;
	}

	public static PointTransaction earnCancel(Member member, Payment payment, Long amount) {
		PointTransaction pointTransaction = new PointTransaction();
		pointTransaction.member = member;
		pointTransaction.payment = payment;
		pointTransaction.transactionType = TransactionType.EARN_CANCEL;
		pointTransaction.amount = amount;
		return pointTransaction;
	}

	public static PointTransaction useRestore(Member member, Payment payment, Long amount) {
		PointTransaction pointTransaction = new PointTransaction();
		pointTransaction.member = member;
		pointTransaction.payment = payment;
		pointTransaction.transactionType = TransactionType.USE_RESTORE;
		pointTransaction.amount = amount;
		return pointTransaction;
	}
}
