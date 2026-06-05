package com.example.team3trimcommercepaymentproject.domain.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.team3trimcommercepaymentproject.domain.member.dto.MyPointResponse;
import com.example.team3trimcommercepaymentproject.domain.member.entity.Member;
import com.example.team3trimcommercepaymentproject.domain.member.repository.MemberRepository;
import com.example.team3trimcommercepaymentproject.global.exception.BusinessException;
import com.example.team3trimcommercepaymentproject.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

	private final MemberRepository memberRepository;

	public Member findMemberEntity(Long memberId) {
		return memberRepository.findById(memberId).orElseThrow(
			() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND)
		);
	}

	public Member findMemberEntityWithLock(Long memberId) {
		return memberRepository.findByIdWithLock(memberId).orElseThrow(
			() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND)
		);
	}

	public Member addPoint(Long memberId, Long amount) {
		Member member = findMemberEntityWithLock(memberId);
		member.addPoint(amount);
		return member;
	}

	public Member usePoint(Long memberId, Long amount) {
		Member member = findMemberEntityWithLock(memberId);
		member.usePoint(amount);
		return member;
	}

	@Transactional(readOnly = true)
	public MyPointResponse getMyPoint(Long memberId) {
		Member member = findMemberEntity(memberId);

		return new MyPointResponse(member.getId(), member.getEmail(), member.getPoint(), member.getModifiedAt());
	}
}
