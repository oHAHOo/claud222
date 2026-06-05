package com.example.team3trimcommercepaymentproject.global.dto;

import org.springframework.data.domain.Page;

/**
 * 페이징 정보를 담는 DTO
 *
 * @param currentPage   현재 페이지
 * @param pageSize      페이지당 개수
 * @param totalElements 전체 개수
 * @param totalPages    전체 페이지 수
 */
public record PageInfo(int currentPage, int pageSize, long totalElements, long totalPages) {
	public static PageInfo from(Page<?> page) {
		return new PageInfo(
			page.getNumber() + 1, page.getSize(),
			page.getTotalElements(), page.getTotalPages()
		);
	}
}
