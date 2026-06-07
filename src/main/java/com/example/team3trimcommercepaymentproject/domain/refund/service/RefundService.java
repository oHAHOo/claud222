package com.example.team3trimcommercepaymentproject.domain.refund.service;

import com.example.team3trimcommercepaymentproject.domain.member.entity.Member;
import com.example.team3trimcommercepaymentproject.domain.member.service.MemberService;
import com.example.team3trimcommercepaymentproject.domain.order.entity.Order;
import com.example.team3trimcommercepaymentproject.domain.order.entity.OrderStatus;
import com.example.team3trimcommercepaymentproject.domain.order.service.OrderService;
import com.example.team3trimcommercepaymentproject.domain.orderItem.entity.OrderItem;
import com.example.team3trimcommercepaymentproject.domain.payment.entity.Payment;
import com.example.team3trimcommercepaymentproject.domain.payment.entity.PaymentStatus;
import com.example.team3trimcommercepaymentproject.domain.payment.repository.PaymentRepository;
import com.example.team3trimcommercepaymentproject.domain.pointTransaction.service.PointTransactionService;
import com.example.team3trimcommercepaymentproject.domain.refund.dto.PartialRefundDTO;
import com.example.team3trimcommercepaymentproject.domain.refund.dto.request.PartialRefundItemRequest;
import com.example.team3trimcommercepaymentproject.domain.refund.dto.request.PartialRefundRequest;
import com.example.team3trimcommercepaymentproject.domain.refund.dto.response.PartialRefundResponse;
import com.example.team3trimcommercepaymentproject.domain.refund.entity.Refund;
import com.example.team3trimcommercepaymentproject.domain.refund.entity.RefundItem;
import com.example.team3trimcommercepaymentproject.domain.refund.repository.RefundItemRepository;
import com.example.team3trimcommercepaymentproject.domain.refund.repository.RefundRepository;
import com.example.team3trimcommercepaymentproject.global.exception.BusinessException;
import com.example.team3trimcommercepaymentproject.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RefundService {

	private final OrderService orderService;
	private final MemberService memberService;
	private final PointTransactionService pointTransactionService;
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

	// 1단계: 부분 환불 가능 여부 선검증 (읽기 트랜잭션)
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

	// 2단계: 부분 환불 DB 갱신 (단일 쓰기 트랜잭션)
	@Transactional
	public PartialRefundDTO processPartialRefund(Long memberId, Long orderId, PartialRefundRequest request) {
		Order order = orderService.getOrderEntity(orderId, memberId);
		Payment payment = order.getPayment();

		Map<Long, OrderItem> itemMap = order.getOrderItems().stream()
			.collect(Collectors.toMap(OrderItem::getId, item -> item));

		// 환불 대상 목록 수집
		List<OrderItem> targetItems = new ArrayList<>();
		List<Integer> targetQty = new ArrayList<>();
		long rawTotalRefund = 0;

		for (PartialRefundItemRequest itemReq : request.items()) {
			OrderItem oi = itemMap.get(itemReq.orderItemId());
			rawTotalRefund += oi.getPriceSnapshot() * itemReq.quantity();
			targetItems.add(oi);
			targetQty.add(itemReq.quantity());
		}

		// 마지막 부분환불 여부 판단 (이번 환불 적용 후 잔여 수량이 전부 0인지 확인)
		boolean isLastRefund = order.getOrderItems().stream().allMatch(oi -> {
			int requested = request.items().stream()
				.filter(r -> r.orderItemId().equals(oi.getId()))
				.mapToInt(PartialRefundItemRequest::quantity)
				.findFirst()
				.orElse(0);
			return oi.getRefundableQuantity() - requested == 0;
		});

		// 마지막 환불 보정: 반올림 누적 오차 제거
		long totalRefundAmount;
		if (isLastRefund) {
			long alreadyRefunded = refundItemRepository.sumRefundedAmountByPaymentIdAndStatus(
				payment.getId(), Refund.RefundStatus.COMPLETED);
			totalRefundAmount = payment.getTotalAmount() - alreadyRefunded;
		} else {
			totalRefundAmount = rawTotalRefund;
		}

		// 환불 금액 분리 산정 (포인트/PG 비율)
		long pointRefundAmount;
		long pgRefundAmount;
		long earnCancelAmount;

		if (payment.getPgAmount() == 0) {
			// 포인트 전액 결제 예외
			pointRefundAmount = totalRefundAmount;
			pgRefundAmount = 0;
			earnCancelAmount = 0;
		} else {
			long rawPoint = totalRefundAmount * payment.getUsedPoint() / payment.getTotalAmount();
			long rawPg = totalRefundAmount * payment.getPgAmount() / payment.getTotalAmount();
			long remainder = totalRefundAmount - rawPoint - rawPg;
			pointRefundAmount = rawPoint;
			pgRefundAmount = rawPg + remainder; // 잔돈은 PG 환불액에 가산

			earnCancelAmount = payment.getEarnedPoint() * pgRefundAmount / payment.getPgAmount();
		}

		// 포인트 부족 정책: 회수할 적립 포인트가 잔액보다 큰 경우 부족분을 PG 환불액에 가산
		if (earnCancelAmount > 0) {
			Member member = memberService.findMemberEntity(memberId);
			if (member.getPoint() < earnCancelAmount) {
				long shortfall = earnCancelAmount - member.getPoint();
				pgRefundAmount += shortfall;
				earnCancelAmount = member.getPoint();
			}
		}

		// 주문 상품 환불 수량 갱신 + 재고 복구
		for (int i = 0; i < targetItems.size(); i++) {
			OrderItem oi = targetItems.get(i);
			int qty = targetQty.get(i);
			oi.refundQuantity(qty);
			oi.getProduct().increaseStock(qty);
		}

		// 결제/주문 상태 전이
		if (isLastRefund) {
			order.cancel(request.reason());
			payment.refund();
		} else {
			payment.partialRefund();
		}

		// 포인트 갱신
		if (earnCancelAmount > 0)
			pointTransactionService.cancelEarnPoint(memberId, payment, earnCancelAmount);
		if (pointRefundAmount > 0)
			pointTransactionService.restoreUsedPoint(memberId, payment, pointRefundAmount);

		// 환불 레코드 저장 (REQUESTED 상태로 시작)
		Refund refund = Refund.builder()
			.payment(payment)
			.reason(request.reason())
			.pointRefundPrice(pointRefundAmount)
			.pgRefundPrice(pgRefundAmount)
			.build();
		refundRepository.save(refund);

		for (int i = 0; i < targetItems.size(); i++) {
			OrderItem oi = targetItems.get(i);
			int qty = targetQty.get(i);
			refundItemRepository.save(RefundItem.builder()
				.refund(refund)
				.orderItem(oi)
				.refundedQuantity(qty)
				.refundedAmount(oi.getPriceSnapshot() * qty)
				.build());
		}

		PartialRefundResponse response = new PartialRefundResponse(
			order.getId(),
			order.getOrderNumber(),
			order.getStatus(),
			payment.getStatus(),
			totalRefundAmount,
			pointRefundAmount,
			pgRefundAmount
		);

		return new PartialRefundDTO(refund.getId(), payment.getPortonePaymentId(), pgRefundAmount, request.reason(), response);
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
