package com.example.team3trimcommercepaymentproject.domain.refund.service;

import com.example.team3trimcommercepaymentproject.domain.order.entity.Order;
import com.example.team3trimcommercepaymentproject.domain.order.entity.OrderStatus;
import com.example.team3trimcommercepaymentproject.domain.order.service.OrderService;
import com.example.team3trimcommercepaymentproject.domain.orderItem.entity.OrderItem;
import com.example.team3trimcommercepaymentproject.domain.payment.entity.Payment;
import com.example.team3trimcommercepaymentproject.domain.payment.repository.PaymentRepository;
import com.example.team3trimcommercepaymentproject.domain.refund.entity.Refund;
import com.example.team3trimcommercepaymentproject.domain.refund.entity.RefundItem;
import com.example.team3trimcommercepaymentproject.domain.refund.repository.RefundItemRepository;
import com.example.team3trimcommercepaymentproject.domain.refund.repository.RefundRepository;
import com.example.team3trimcommercepaymentproject.global.exception.BusinessException;
import com.example.team3trimcommercepaymentproject.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefundService {

	private final OrderService orderService;
	private final PaymentRepository paymentRepository;
	private final RefundRepository refundRepository;
	private final RefundItemRepository refundItemRepository;

	// 1단계: 주문 취소 가능 여부 선검증 (읽기 트랜잭션)
	@Transactional(readOnly = true)
	public void validateCancelable(Long memberId, Long orderId) {
		Order order = orderService.getOrderEntity(memberId, orderId);

		OrderStatus status = order.getStatus();
		if (status != OrderStatus.PAYMENT_PENDING && status != OrderStatus.COMPLETED)
			throw new BusinessException(ErrorCode.ORDER_NOT_CANCELABLE);

		Payment payment = order.getPayment();
		if (payment == null)
			throw new BusinessException(ErrorCode.PAYMENT_NOT_FOUND);
	}



	// 주문 취소 시 환불 이력 저장 (재고·포인트·결제상태는 OrderService가 처리)
	@Transactional
	public Long saveRefund(Long paymentId, String reason) {
		Payment payment = paymentRepository.findById(paymentId).orElseThrow(
			() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND)
		);

		Refund refund = Refund.builder()
			.payment(payment)
			.reason(reason)
			.pointRefundPrice(payment.getUsedPoint())
			.pgRefundPrice(payment.getPgAmount())
			.build();
		refundRepository.save(refund);

		for (OrderItem item : payment.getOrder().getOrderItems()) {
			refundItemRepository.save(RefundItem.builder()
				.refund(refund)
				.orderItem(item)
				.refundedQuantity(item.getQuantity())
				.refundedAmount((long) item.getPriceSnapshot() * item.getQuantity())
				.build());
		}

		return refund.getId();
	}

	@Transactional
	public void completeRefund(Long refundId) {
		Refund refund = refundRepository.findById(refundId).orElseThrow(
			() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND)
		);
		refund.complete();
	}

	@Transactional
	public void failRefund(Long refundId) {
		Refund refund = refundRepository.findById(refundId).orElseThrow(
			() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND)
		);
		refund.fail();
	}
}