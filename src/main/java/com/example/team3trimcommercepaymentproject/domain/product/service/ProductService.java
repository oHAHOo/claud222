package com.example.team3trimcommercepaymentproject.domain.product.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.team3trimcommercepaymentproject.domain.product.dto.ProductDetailGetResponse;
import com.example.team3trimcommercepaymentproject.domain.product.dto.ProductFilter;
import com.example.team3trimcommercepaymentproject.domain.product.dto.ProductListGetResponse;
import com.example.team3trimcommercepaymentproject.domain.product.entity.Product;
import com.example.team3trimcommercepaymentproject.domain.product.repository.ProductRepository;
import com.example.team3trimcommercepaymentproject.global.exception.BusinessException;
import com.example.team3trimcommercepaymentproject.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

	private final ProductRepository productRepository;

	public Product findProductEntity(Long productId) {
		return productRepository.findById(productId).orElseThrow(
			() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND)
		);
	}

	// 다건 조회
	@Transactional(readOnly = true)
	public ProductListGetResponse getAll(Pageable pageable, ProductFilter filter) {
		Page<Product> productPage = productRepository.findAllByFilter(filter, pageable);

		return ProductListGetResponse.from(productPage);
	}

	// 단건 조회
	@Transactional(readOnly = true)
	public ProductDetailGetResponse getProduct(Long productId) {
		Product product = findProductEntity(productId);

		return ProductDetailGetResponse.from(product);
	}

}
