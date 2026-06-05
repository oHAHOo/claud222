package com.example.team3trimcommercepaymentproject.domain.member.entity;

import com.example.team3trimcommercepaymentproject.global.entity.BaseEntity;
import com.example.team3trimcommercepaymentproject.global.exception.BusinessException;
import com.example.team3trimcommercepaymentproject.global.exception.ErrorCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

	@Column(unique = true)
	private String email;
	@Column
	private String encryptedPassword;
	@Column
	private String name;
	@Column
	private String phoneNumber;
	@Column(columnDefinition = "BIGINT DEFAULT 0")
	private Long point = 0L;

	public Member(String email, String encryptedPassword, String name, String phoneNumber) {
		this.email = email;
		this.encryptedPassword = encryptedPassword;
		this.name = name;
		this.phoneNumber = phoneNumber;
	}

	public void addPoint(Long amount) {
		this.point += amount;
	}

	public void usePoint(Long amount) {
		if (this.point < amount) {
			throw new BusinessException(ErrorCode.POINT_NOT_ENOUGH);
		}
		this.point -= amount;
	}
}
