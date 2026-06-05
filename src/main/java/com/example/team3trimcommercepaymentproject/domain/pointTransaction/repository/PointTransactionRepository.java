package com.example.team3trimcommercepaymentproject.domain.pointTransaction.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.team3trimcommercepaymentproject.domain.pointTransaction.entity.PointTransaction;

@Repository
public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {
	List<PointTransaction> findAllByMemberIdOrderByModifiedAtDesc(Long memberId);
}
