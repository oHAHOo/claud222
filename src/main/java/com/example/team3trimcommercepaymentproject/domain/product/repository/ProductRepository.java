package com.example.team3trimcommercepaymentproject.domain.product.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.team3trimcommercepaymentproject.domain.product.dto.ProductFilter;
import com.example.team3trimcommercepaymentproject.domain.product.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

	@Query("SELECT p FROM Product p " +
		"WHERE (:#{#filter.categoryName()} IS NULL OR p.category.name LIKE CONCAT('%', :#{#filter.categoryName()}, '%')) " +
		"AND (:#{#filter.minPrice()} IS NULL OR p.price >= :#{#filter.minPrice()}) " +
		"AND (:#{#filter.maxPrice()} IS NULL OR p.price <= :#{#filter.maxPrice()}) " +
		"AND (:#{#filter.status()} IS NULL OR p.saleStatus = :#{#filter.status()})")
	Page<Product> findAllByFilter(
		@Param("filter") ProductFilter filter,
		Pageable pageable
	);

}
