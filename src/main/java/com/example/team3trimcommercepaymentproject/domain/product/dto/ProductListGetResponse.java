package com.example.team3trimcommercepaymentproject.domain.product.dto;

import java.util.List;

import org.springframework.data.domain.Page;

import com.example.team3trimcommercepaymentproject.domain.product.entity.Product;
import com.example.team3trimcommercepaymentproject.global.dto.PageInfo;

public record ProductListGetResponse(
	List<ProductPageGetResponse> products,
	PageInfo pageInfo
) {
	public static ProductListGetResponse from(Page<Product> pageProduct) {
		List<ProductPageGetResponse> products =pageProduct.stream()
			.map(ProductPageGetResponse::from).toList();

		return new ProductListGetResponse(products, PageInfo.from(pageProduct));
	}
}
