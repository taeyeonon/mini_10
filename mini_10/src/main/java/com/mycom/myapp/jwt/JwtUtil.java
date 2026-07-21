package com.mycom.myapp.jwt;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.mycom.myapp.config.MyUserDetailsService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Component
@RequiredArgsConstructor
@Getter
@Slf4j
public class JwtUtil {

	private final MyUserDetailsService myUserDetailsService;
	
	// JWT 서명/검증용 시크릿 키 (application.properties의 myapp.jwt.secret에서 로드)
	@Value("${myapp.jwt.secret}")
	private String secretKeyStr;
	
	private SecretKey secretKey; // HS256 서명/검증 키
	
	private final long tokenValidDuration = 1000L * 60 * 60 * 24; // 24시간
	
	// JwtUtil 생성 직후 호출
	@PostConstruct
	protected void init() {
		secretKey = new SecretKeySpec(
				secretKeyStr.getBytes(StandardCharsets.UTF_8),
				Jwts.SIG.HS256.key().build().getAlgorithm()
		);
	}
	
	// JWT 생성: 사용자명과 권한 목록을 포함한 토큰 발급
	public String createToken(String username, List<String> roles) {
		Date now = new Date();
		
		return Jwts.builder()
				.subject(username)  // 페이로드: 사용자 식별자
				.claim("roles", roles) // 페이로드: 사용자 권한 목록
				.issuedAt(now)  // 페이로드: 발급 시각
				.expiration(new Date(now.getTime() + tokenValidDuration))
				.signWith(secretKey, Jwts.SIG.HS256)
				.compact();
	}
	
	// JWT에서 사용자명 추출
	public String getUsernameFromToken(String token) {
		return Jwts.parser()
				.verifyWith(secretKey) // 토큰의 서명 검증
				.build()
				.parseSignedClaims(token)
				.getPayload()
				.getSubject();
	}
	
	// 토큰 추출: 프론트에서 X-AUTH-TOKEN 헤더로 전달한 JWT 토큰 추출
	public String getTokenFromHeader(HttpServletRequest request) {
		return request.getHeader("X-AUTH-TOKEN");
	}
	
	// 토큰 서명 및 만료 검증
	public Claims validateToken(String token) {
		try {
			Claims claims = Jwts.parser()
					.verifyWith(secretKey)
					.build()
					.parseSignedClaims(token)
					.getPayload();
			
			if( claims.getExpiration() != null && claims.getExpiration().before(new Date())) {
				// 토큰 만료됨
				return null;
			}
			
			return claims; // 유효함
			
		}catch(Exception e) {
			return null;
		}
	}
	
	// DB 기반 2차 검증: 토큰에서 추출한 사용자명으로 DB에서 최신 정보 조회 및 인증 객체 생성
	public UsernamePasswordAuthenticationToken getAuthentication(String token) {
		UserDetails userDetails = myUserDetailsService.loadUserByUsername(getUsernameFromToken(token));
		return new UsernamePasswordAuthenticationToken(
				userDetails, 
				"", 
				userDetails.getAuthorities());
	}
}







