package com.example.team3trimcommercepaymentproject.domain.product.dto;

import java.time.LocalDateTime;

import com.example.team3trimcommercepaymentproject.domain.product.entity.Product;

public record ProductPageGetResponse(
	Long id,
	String name,
	Integer price,
	Integer stockQuantity,
	String categoryName,
	Product.SaleStatus saleStatus,
	LocalDateTime createdAt
) {

	public static ProductPageGetResponse from(Product product) {
		return new ProductPageGetResponse(
			product.getId(),
			product.getName(),
			product.getPrice(),
			product.getStockQuantity(),
			product.getCategory().getName(),
			product.getSaleStatus(),
			product.getCreatedAt()
		);
	}

}
