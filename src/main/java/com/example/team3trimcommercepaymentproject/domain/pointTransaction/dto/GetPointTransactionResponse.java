package com.example.team3trimcommercepaymentproject.domain.pointTransaction.dto;

import java.time.LocalDateTime;

import com.example.team3trimcommercepaymentproject.domain.pointTransaction.entity.PointTransaction;
import com.example.team3trimcommercepaymentproject.domain.pointTransaction.entity.TransactionType;

public record GetPointTransactionResponse(
	Long id,
	Long memberId,
	Long paymentId,
	TransactionType transactionType,
	Long amount,
	LocalDateTime createdAt,
	LocalDateTime modifiedAt
) {
	public static GetPointTransactionResponse from(PointTransaction pointTransaction) {
		return new GetPointTransactionResponse(
			pointTransaction.getId(),
			pointTransaction.getMember().getId(),
			pointTransaction.getPayment().getId(),
			pointTransaction.getTransactionType(),
			pointTransaction.getAmount(),
			pointTransaction.getCreatedAt(),
			pointTransaction.getModifiedAt()
		);
	}
}
