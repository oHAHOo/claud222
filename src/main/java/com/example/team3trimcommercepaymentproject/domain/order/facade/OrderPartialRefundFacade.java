package com.example.team3trimcommercepaymentproject.domain.order.facade;

import com.example.team3trimcommercepaymentproject.domain.order.dto.PartialRefundDTO;
import com.example.team3trimcommercepaymentproject.domain.order.dto.request.PartialRefundRequest;
import com.example.team3trimcommercepaymentproject.domain.order.dto.response.PartialRefundResponse;
import com.example.team3trimcommercepaymentproject.domain.order.service.OrderService;
import com.example.team3trimcommercepaymentproject.domain.payment.portOne.PortOneClient;
import com.example.team3trimcommercepaymentproject.domain.refund.service.RefundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPartialRefundFacade {

	private final OrderService orderService;
	private final RefundService refundService;
	private final PortOneClient portOneClient;

	public PartialRefundResponse partialRefund(Long memberId, Long orderId, PartialRefundRequest request) {
		// 1단계: 선검증 (트랜잭션 밖)
		refundService.validatePartialRefundable(memberId, orderId);

		// 2단계: DB 갱신 단일 트랜잭션 (환불 금액 산정 + 포인트/재고/상태 처리)
		PartialRefundDTO dto = orderService.partialRefund(memberId, orderId, request);

		// 3단계: 환불 이력 저장
		Long refundId = refundService.savePartialRefund(dto);

		// 4단계: PG 취소 요청 (트랜잭션 밖, PG 환불액이 있을 때만)
		if (dto.pgRefundAmount() > 0) {
			try {
				portOneClient.cancelPaymentPartial(dto.portonePaymentId(), dto.cancelReason(), dto.pgRefundAmount());
			} catch (Exception e) {
				log.error("[PG 부분취소 실패] orderId={}, portonePaymentId={}, refundId={}, pgAmount={}, error={}",
					orderId, dto.portonePaymentId(), refundId, dto.pgRefundAmount(), e.getMessage(), e);
				refundService.failRefund(refundId);
				return dto.response();
			}
		}

		refundService.completeRefund(refundId);
		return dto.response();
	}
}
