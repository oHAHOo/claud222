package com.example.team3trimcommercepaymentproject.domain.auth.service;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.team3trimcommercepaymentproject.domain.auth.dto.LoginRequest;
import com.example.team3trimcommercepaymentproject.domain.auth.dto.LoginResponse;
import com.example.team3trimcommercepaymentproject.domain.auth.dto.SignupRequest;
import com.example.team3trimcommercepaymentproject.domain.auth.dto.SignupResponse;
import com.example.team3trimcommercepaymentproject.domain.member.entity.Member;
import com.example.team3trimcommercepaymentproject.domain.member.repository.MemberRepository;
import com.example.team3trimcommercepaymentproject.global.exception.BusinessException;
import com.example.team3trimcommercepaymentproject.global.exception.ErrorCode;
import com.example.team3trimcommercepaymentproject.global.jwt.JwtProvider;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtProvider jwtProvider;

	@Transactional
	public SignupResponse signup(@Valid SignupRequest request) {
		if (memberRepository.existsByEmail(request.email())) {
			throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
		}
		Member member = new Member(
			request.email(),
			passwordEncoder.encode(request.password()),
			request.name(),
			request.phoneNumber()
		);
		memberRepository.save(member);
		return new SignupResponse(
			member.getId(),
			member.getEmail(),
			member.getName(),
			member.getPhoneNumber(),
			LocalDateTime.now()
		);
	}

	@Transactional
	public LoginResponse login(LoginRequest request) {
		Member member = memberRepository.findByEmail(request.email())
			.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

		if (!passwordEncoder.matches(request.password(), member.getEncryptedPassword())) {
			throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
		}
		String token = jwtProvider.createToken(member.getId(), member.getEmail());
		return new LoginResponse(token, "Bearer", jwtProvider.getExpiresIn(), toMemberInfo(member));
	}

	private LoginResponse.MemberInfo toMemberInfo(Member member) {
		return new LoginResponse.MemberInfo(member.getId(), member.getEmail(), member.getName());
	}
}
