package com.example.team3trimcommercepaymentproject.domain.product.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.team3trimcommercepaymentproject.domain.product.dto.ProductDetailGetResponse;
import com.example.team3trimcommercepaymentproject.domain.product.dto.ProductFilter;
import com.example.team3trimcommercepaymentproject.domain.product.dto.ProductListGetResponse;
import com.example.team3trimcommercepaymentproject.domain.product.entity.Product;
import com.example.team3trimcommercepaymentproject.domain.product.service.ProductService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {

	private final ProductService productService;

	// 다건 조회: 조건 필터 + 페이지네이션으로 상품 목록을 조회
	@GetMapping
	public ResponseEntity<ProductListGetResponse> getAllProduct(
		@RequestParam(required = false) String categoryName,
		@RequestParam(required = false) Integer minPrice,
		@RequestParam(required = false) Integer maxPrice,
		@RequestParam(required = false) Product.SaleStatus status,
		@PageableDefault(page = 1, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
	) {
		int page = Math.max(pageable.getPageNumber() - 1, 0);
		int size = Math.min(Math.max(pageable.getPageSize(), 1), 100);
		pageable = PageRequest.of(page, size, pageable.getSort());

		ProductFilter productFilter = new ProductFilter(categoryName, minPrice, maxPrice, status);
		ProductListGetResponse response = productService.getAll(pageable, productFilter);

		return ResponseEntity.ok(response);
	}

	// 단건 조회
	@GetMapping("/{productId}")
	public ResponseEntity<ProductDetailGetResponse> getProduct(
		@PathVariable Long productId
	) {
		ProductDetailGetResponse response = productService.getProduct(productId);

		return ResponseEntity.ok(response);
	}

}
