package com.example.team3trimcommercepaymentproject.domain.pointTransaction.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.team3trimcommercepaymentproject.domain.member.entity.Member;
import com.example.team3trimcommercepaymentproject.domain.member.service.MemberService;
import com.example.team3trimcommercepaymentproject.domain.payment.entity.Payment;
import com.example.team3trimcommercepaymentproject.domain.pointTransaction.dto.GetPointTransactionResponse;
import com.example.team3trimcommercepaymentproject.domain.pointTransaction.entity.PointTransaction;
import com.example.team3trimcommercepaymentproject.domain.pointTransaction.repository.PointTransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PointTransactionService {

	private final MemberService memberService;
	private final PointTransactionRepository pointTransactionRepository;

	public void earnPoint(Long memberId, Payment payment, Long amount) {
		Member member = memberService.addPoint(memberId, amount);
		pointTransactionRepository.save(PointTransaction.earn(member, payment, amount));
	}

	public void usePoint(Long memberId, Payment payment, Long amount) {
		Member member = memberService.usePoint(memberId, amount);
		pointTransactionRepository.save(PointTransaction.use(member, payment, amount));
	}

	public void cancelEarnPoint(Long memberId, Payment payment, Long amount) {
		Member member = memberService.usePoint(memberId, amount);
		pointTransactionRepository.save(PointTransaction.earnCancel(member, payment, amount));
	}

	public void restoreUsedPoint(Long memberId, Payment payment, Long amount) {
		Member member = memberService.addPoint(memberId, amount);
		pointTransactionRepository.save(PointTransaction.useRestore(member, payment, amount));
	}

	public List<GetPointTransactionResponse> getPointTransaction(Long memberId) {
		return pointTransactionRepository.findAllByMemberIdOrderByModifiedAtDesc(memberId)
			.stream()
			.map(GetPointTransactionResponse::from)
			.toList();
	}
}
