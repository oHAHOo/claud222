package com.example.team3trimcommercepaymentproject.domain.product.dto;

import com.example.team3trimcommercepaymentproject.domain.product.entity.Product;

public record ProductFilter(
	String categoryName,
	Integer minPrice,
	Integer maxPrice,
	Product.SaleStatus status
) {
}
