package com.mycom.myapp.config;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Builder;
import lombok.Getter;

// Spring Security의 UserDetails 인터페이스 구현
// @Getter로 getter 메소드 자동 생성 및 추상 메소드 구현
@Builder
@Getter
public class MyUserDetails implements UserDetails{

	private static final long serialVersionUID = 1L;
	
	// Spring Security 필수 필드
	private final String username;  // 로그인 식별자 (이메일)
	private final String password;  // BCrypt로 인코딩된 비밀번호
	private final Collection<? extends GrantedAuthority> authorities;  // 사용자 권한 목록
	
	// 비즈니스 로직용 추가 필드
	private final Long id;
	private final String name;
	private final String email;

}










