package com.mycom.myapp.jwt;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// OncePerRequestFilter: 요청 한 개당 한 번만 수행
// JWT 인증 필터: DB 기반 2차 검증 방식
// - 토큰 서명/만료 검증 + DB에서 사용자 정보 확인
// - 토큰 발급 후 사용자 탈퇴 등의 상황을 항상 최신화하여 반영
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter{

	private final JwtUtil jwtUtil;
	
	@Override
	protected void doFilterInternal(
			HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

		// 요청 헤더에서 JWT 토큰 추출
		String token = jwtUtil.getTokenFromHeader(request); // 없으면 token 은 null
		
		// 토큰 서명/만료 검증
		Claims claims = (token != null ) ? jwtUtil.validateToken(token) : null;
		
		// DB 기반 2차 검증: 사용자 정보가 DB에 존재하고 유효한지 확인
		if( claims != null ) {
			UsernamePasswordAuthenticationToken authenticationToken = 
					jwtUtil.getAuthentication(token);
			
			authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
			// SecurityContextHolder에 인증 정보 설정 (필터 체인 처리 중 사용)
			SecurityContextHolder.getContext().setAuthentication(authenticationToken); 
		}
		
		filterChain.doFilter(request, response);		
	}
}
