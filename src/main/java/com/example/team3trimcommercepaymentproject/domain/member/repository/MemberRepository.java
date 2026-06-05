package com.example.team3trimcommercepaymentproject.domain.member.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.team3trimcommercepaymentproject.domain.member.entity.Member;

import jakarta.persistence.LockModeType;

public interface MemberRepository extends JpaRepository<Member, Long> {

	boolean existsByEmail(String email);

	Optional<Member> findByEmail(String email);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT m FROM Member m WHERE m.id = :memberId")
	Optional<Member> findByIdWithLock(@Param("memberId") Long memberId);
}
