package com.mycom.myapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * ═══════════════════════════════════════════════════════════════
 * SecurityConfig (Spring Security 설정)
 * ═══════════════════════════════════════════════════════════════
 *
 * [이 클래스가 필요한 이유]
 * ① PasswordEncoder(BCrypt) Bean 등록
 *    - UserServiceImpl이 주입받아 비밀번호 암호화에 사용
 *    - 이 Bean이 없으면 앱이 시작조차 안 됨! (주입 실패)
 * ② 어떤 URL을 로그인 없이 허용할지 결정
 *    - spring-boot-starter-security를 넣는 순간 "모든" 요청이 잠기기 때문에
 *      회원가입(/users) 같은 API는 직접 열어줘야(permitAll) 함
 *
 * ※ 추후 JWT 로그인을 붙일 때 이 파일에 JWT 필터 설정을 추가하면 됨
 *   (build.gradle에 jjwt 의존성은 이미 준비되어 있음)
 */
@Configuration	// "이 클래스는 설정 담당"이라고 Spring에 등록
public class SecurityConfig {

	/**
	 * 비밀번호 암호화 도구 등록 (BCrypt)
	 *
	 * [@Bean이란?]
	 * - 이 메서드가 반환하는 객체를 Spring이 관리하게 함
	 * - UserServiceImpl의 "private final PasswordEncoder passwordEncoder"에
	 *   Spring이 이 객체를 자동으로 넣어줌 (의존성 주입)
	 *
	 * [BCrypt의 특징]
	 * - 일방향 암호화: "password123" → "$2a$10$..." (원문 복구 불가!)
	 * - 같은 비밀번호도 매번 다른 결과로 암호화됨 (salt 자동 포함)
	 * - 그래서 비교는 equals가 아니라 passwordEncoder.matches()로만 가능
	 */
	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	/**
	 * URL별 접근 규칙 설정 (보안 필터 체인)
	 *
	 * [현재 설정 요약]
	 * - REST API 프로젝트라서 폼 로그인/세션 방식은 전부 끔
	 * - /users/** 는 로그인 없이 허용 (회원가입/조회 테스트를 위해)
	 * - 그 외 요청은 로그인(인증) 필요
	 */
	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		return http
				// REST API 방식이므로 브라우저 팝업 로그인(basic), 폼 로그인 사용 안 함
				.httpBasic(httpBasic -> httpBasic.disable())
				.formLogin(formLogin -> formLogin.disable())
				// CSRF: 세션 기반 웹의 위조 요청 방지 기능 → 세션을 안 쓰는 REST API는 끔
				.csrf(csrf -> csrf.disable())
				// 세션을 만들지 않음 (추후 JWT 같은 토큰 방식과 어울리는 설정)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				// URL별 접근 권한 설정
				.authorizeHttpRequests(request -> request
						.requestMatchers(
							"/",
							"/index.html",
							"/users/**"		// User API는 로그인 없이 허용 (회원가입 포함)
						).permitAll()
						.anyRequest().authenticated()	// 나머지는 로그인 필요
				)
				.build();
	}
}
