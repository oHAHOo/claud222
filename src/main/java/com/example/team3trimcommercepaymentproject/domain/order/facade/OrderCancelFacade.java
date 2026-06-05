package com.example.team3trimcommercepaymentproject.domain.order.facade;

import com.example.team3trimcommercepaymentproject.domain.order.dto.OrderCancelDTO;
import com.example.team3trimcommercepaymentproject.domain.order.dto.request.OrderCancelRequest;
import com.example.team3trimcommercepaymentproject.domain.order.dto.response.OrderCancelResponse;
import com.example.team3trimcommercepaymentproject.domain.order.service.OrderService;
import com.example.team3trimcommercepaymentproject.domain.payment.portOne.PortOneClient;
import com.example.team3trimcommercepaymentproject.domain.refund.service.RefundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCancelFacade {

	private final OrderService orderService;
	private final RefundService refundService;
	private final PortOneClient portOneClient;

	public OrderCancelResponse cancel(Long memberId, Long orderId, OrderCancelRequest request) {
		// 1단계: 취소 가능 여부 선검증 (RefundService 담당, readOnly 트랜잭션)
		refundService.validateCancelable(memberId, orderId);

		// 2단계: 주문 취소 DB 갱신 (OrderService 담당, 쓰기 트랜잭션 커밋)
		OrderCancelDTO dto = orderService.cancel(memberId, orderId, request);

		// PAYMENT_PENDING 취소는 PG 결제가 없으므로 여기서 종료
		if (!dto.needsPgCancel()) {
			return dto.response();
		}

		// 3단계: 환불 이력 저장 (RefundService 담당, 쓰기 트랜잭션 커밋)
		Long refundId = refundService.saveRefund(dto.paymentId(), dto.cancelReason());

		// 4단계: PG 취소 요청 (트랜잭션 밖)
		try {
			portOneClient.cancelPayment(dto.portonePaymentId(), dto.cancelReason());
		} catch (Exception e) {
			log.error("[PG 취소 실패] orderId={}, portonePaymentId={}, refundId={}, error={}",
				orderId, dto.portonePaymentId(), refundId, e.getMessage(), e);
			refundService.failRefund(refundId);
			return dto.response();
		}

		// 4단계: 환불 완료 처리 (RefundService 담당)
		refundService.completeRefund(refundId);

		return dto.response();
	}
}