package com.mycom.myapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.mycom.myapp.jwt.JwtAuthenticationFilter;
import com.mycom.myapp.jwt.JwtUtil;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtUtil jwtUtil;
	
	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}
	
	@Bean
	SecurityFilterChain filterChain(
			HttpSecurity http, 
			MyAuthenticationEntryPoint entryPoint,
			MyAccessDeniedHandler accessDeniedHandler
	) throws Exception {
		return http
				.httpBasic(httpBasic -> httpBasic.disable())
				.formLogin(formLogin -> formLogin.disable())
				.csrf(csrf -> csrf.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(request -> request
										.requestMatchers(
											"/", 
											"/index.html",
											"/.well-known/**",
											"/assets/**",
											"/board.html",
											"/login",
											"/login.html",
											"/register",
											"/register.html",
											"/users/**",
											"/auth/**",
											// 정적 화면은 모두 공개하고, 실제 권한 검사는 아래 API 규칙에서 한다.
											// (화면 안의 requireAuth() 는 사용자 흐름용 1차 가드일 뿐이다.)
											"/admin.html",
											"/admin-dashboard.html",
											"/admin-payment.html",
											"/admin-schedule.html",
											"/admin-user.html",
											"/admin-settings.html",
											"/customer.html",
											"/trainer.html",
											"/trainer-dashboard.html",
											"/trainer-template.html",
											"/trainer-reservation.html",
											"/trainer-settings.html"
										).permitAll()
										
										// 태연님: 트레이너 전용 일정 API (TRAINER 권한)
										.requestMatchers(
											"/api/trainer/schedules/**",
											"/api/trainer/schedule-templates/**"
										).hasRole("TRAINER")
										
										// 태연님: 회원용 일정 GET 요청 (CUSTOMER, TRAINER, ADMIN 권한)
										.requestMatchers(HttpMethod.GET, "/api/schedules/**")
										.hasAnyRole("CUSTOMER", "TRAINER", "ADMIN")
										
										.requestMatchers("/customer/**").hasAnyRole("CUSTOMER", "ADMIN")
										.requestMatchers("/admin/**", "/api/admin/**").hasRole("ADMIN")
										.anyRequest().authenticated()
				)
				.exceptionHandling(exceptionHandling -> exceptionHandling
						.authenticationEntryPoint(entryPoint) // 401 미인증 (로그인 필요)
						.accessDeniedHandler(accessDeniedHandler) // 403 권한없음 추가
				)
				.addFilterBefore(new JwtAuthenticationFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class)
				.build();
	}	
}
