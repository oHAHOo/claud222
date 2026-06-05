package com.example.team3trimcommercepaymentproject.domain.product.entity;

import com.example.team3trimcommercepaymentproject.global.entity.BaseEntity;

import com.example.team3trimcommercepaymentproject.global.exception.BusinessException;
import com.example.team3trimcommercepaymentproject.global.exception.ErrorCode;
import jakarta.persistence.FetchType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "products")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// 상품은 하나의 카테고리에 속한다.
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id", nullable = false)
	private Category category;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private Integer price;

	@Column(name = "stock_quantity", nullable = false)
	private Integer stockQuantity;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String description;

	//Enum 유지 + DB 문자열 저장
	@Enumerated(EnumType.STRING)
	@Column(name = "sale_status", nullable = false, length = 20)
	private SaleStatus saleStatus;

	//생성자 헷갈림 방지
	@Builder
	private Product(
		Category category,
		String name,
		Integer price,
		Integer stockQuantity,
		String description,
		SaleStatus saleStatus
	) {
		this.category = category;
		this.name = name;
		this.price = price;
		this.stockQuantity = stockQuantity;
		this.description = description;
		this.saleStatus = saleStatus;
	}

	public void decreaseStock(int quantity) {
		if (quantity <= 0) {
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		}
		if (this.stockQuantity < quantity) {
			throw new BusinessException(ErrorCode.OUT_OF_STOCK);
		}
		this.stockQuantity -= quantity;
	}

	public void increaseStock(int quantity) {
		if (quantity <= 0) {
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		}
		this.stockQuantity += quantity;
	}

	public enum SaleStatus {
		ON_SALE,
		SOLD_OUT
	}
}
