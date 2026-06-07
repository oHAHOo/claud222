package com.example.team3trimcommercepaymentproject.domain.refund.facade;

import com.example.team3trimcommercepaymentproject.domain.payment.portOne.PortOneClient;
import com.example.team3trimcommercepaymentproject.domain.refund.dto.PartialRefundDTO;
import com.example.team3trimcommercepaymentproject.domain.refund.dto.request.PartialRefundRequest;
import com.example.team3trimcommercepaymentproject.domain.refund.dto.response.PartialRefundResponse;
import com.example.team3trimcommercepaymentproject.domain.refund.service.RefundService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PartialRefundFacade {

	private final RefundService refundService;
	private final PortOneClient portOneClient;

	public PartialRefundResponse refund(Long memberId, Long orderId, PartialRefundRequest request) {
		// 1단계: 선검증 (읽기 전용, 트랜잭션 밖)
		refundService.validatePartialRefundable(memberId, orderId, request);

		// 2단계: DB 갱신 (단일 트랜잭션 커밋)
		PartialRefundDTO dto = refundService.processPartialRefund(memberId, orderId, request);

		// 3단계: PG 취소 요청 (트랜잭션 밖, PG 금액이 있을 때만)
		if (dto.pgRefundAmount() > 0) {
			try {
				portOneClient.cancelPayment(dto.portonePaymentId(), dto.pgRefundAmount(), dto.reason());
			} catch (Exception e) {
				log.error("[부분환불 PG 취소 실패] orderId={}, portonePaymentId={}, refundId={}, pgAmount={}, error={}",
					orderId, dto.portonePaymentId(), dto.refundId(), dto.pgRefundAmount(), e.getMessage(), e);
				refundService.failRefund(dto.refundId());
				return dto.response();
			}
		}

		refundService.completeRefund(dto.refundId());
		return dto.response();
	}
}
