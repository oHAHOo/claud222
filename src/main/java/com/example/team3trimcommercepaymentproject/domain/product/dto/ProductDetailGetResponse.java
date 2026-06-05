package com.example.team3trimcommercepaymentproject.domain.product.dto;

import java.time.LocalDateTime;

import com.example.team3trimcommercepaymentproject.domain.product.entity.Product;

public record ProductDetailGetResponse(
	Long id,
	String name,
	Integer price,
	Integer stockQuantity,
	String description,
	String categoryName,
	Product.SaleStatus saleStatus,
	LocalDateTime createdAt,
	LocalDateTime modifiedAt
) {

	public static ProductDetailGetResponse from(Product product) {
		return new ProductDetailGetResponse(
			product.getId(),
			product.getName(),
			product.getPrice(),
			product.getStockQuantity(),
			product.getDescription(),
			product.getCategory().getName(),
			product.getSaleStatus(),
			product.getCreatedAt(),
			product.getModifiedAt()
		);
	}
}
