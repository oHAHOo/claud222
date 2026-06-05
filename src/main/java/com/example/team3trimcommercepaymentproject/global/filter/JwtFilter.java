package com.example.team3trimcommercepaymentproject.global.filter;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.team3trimcommercepaymentproject.global.jwt.JwtProvider;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

	private final JwtProvider jwtProvider;

	protected void doFilterInternal(HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {

		String header = request.getHeader("Authorization");

		if (header != null && header.startsWith("Bearer ")) {
			String token = header.substring(7);
			if (jwtProvider.validate(token)) {
				Long memberId = jwtProvider.getMemberId(token);
				UsernamePasswordAuthenticationToken auth =
					new UsernamePasswordAuthenticationToken(memberId, null, null);
				SecurityContextHolder.getContext().setAuthentication(auth);
			} else {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.setContentType("application/json");
				response.getWriter().write(
					"{\"status\":401,\"message\":\"유효하지 않은 토큰입니다\"}"

				);
				return;
			}
		}
		filterChain.doFilter(request,response);
	}
}
