package com.example.team3trimcommercepaymentproject.domain.refund.service;

import com.example.team3trimcommercepaymentproject.domain.order.dto.request.PartialRefundRequest;
import com.example.team3trimcommercepaymentproject.domain.order.dto.request.PartialRefundItemRequest;
import com.example.team3trimcommercepaymentproject.domain.order.entity.Order;
import com.example.team3trimcommercepaymentproject.domain.order.entity.OrderStatus;
import com.example.team3trimcommercepaymentproject.domain.order.service.OrderService;
import com.example.team3trimcommercepaymentproject.domain.orderItem.entity.OrderItem;
import com.example.team3trimcommercepaymentproject.domain.payment.entity.Payment;
import com.example.team3trimcommercepaymentproject.domain.payment.entity.PaymentStatus;
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

import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RefundService {

	private final OrderService orderService;
	private final PaymentRepository paymentRepository;
	private final RefundRepository refundRepository;
	private final RefundItemRepository refundItemRepository;

	// 주문 전액 취소 가능 여부 선검증 (읽기 트랜잭션)
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

	// 부분 환불 가능 여부 선검증 (읽기 트랜잭션)
	@Transactional(readOnly = true)
	public void validatePartialRefundable(Long memberId, Long orderId, PartialRefundRequest request) {
		if (request.items() == null || request.items().isEmpty())
			throw new BusinessException(ErrorCode.REFUND_ITEMS_EMPTY);

		Order order = orderService.getOrderEntity(orderId, memberId);
		Payment payment = order.getPayment();

		if (payment == null)
			throw new BusinessException(ErrorCode.PAYMENT_NOT_FOUND);

		PaymentStatus status = payment.getStatus();
		if (status != PaymentStatus.PAID && status != PaymentStatus.PARTIAL_REFUNDED)
			throw new BusinessException(ErrorCode.ORDER_NOT_REFUNDABLE);

		Map<Long, OrderItem> itemMap = order.getOrderItems().stream()
			.collect(Collectors.toMap(OrderItem::getId, item -> item));

		for (PartialRefundItemRequest itemReq : request.items()) {
			OrderItem orderItem = itemMap.get(itemReq.orderItemId());
			if (orderItem == null)
				throw new BusinessException(ErrorCode.ORDER_ITEM_NOT_FOUND);
			if (itemReq.quantity() > orderItem.getRefundableQuantity())
				throw new BusinessException(ErrorCode.REFUND_QUANTITY_EXCEEDED);
		}
	}

	// 주문 전액 취소 시 환불 이력 저장 (재고·포인트·결제상태는 OrderService가 처리)
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
