package com.mycom.myapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
			MyAccessDeniedHandler accessDeniedHandler // 👈 1. 여기에 403 핸들러 파라미터 추가!
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
											"/auth/**"
										).permitAll()
										.requestMatchers("/customer/**").hasAnyRole("CUSTOMER", "ADMIN")
										.requestMatchers("/admin/**").hasRole("ADMIN")
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