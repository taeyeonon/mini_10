package com.mycom.myapp.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.mycom.myapp.auth.dto.LoginResultDto;
import com.mycom.myapp.jwt.JwtUtil;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

@ExtendWith(MockitoExtension.class)
class LoginServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private Authentication authentication;

    private LoginServiceImpl loginService;

    @BeforeEach
    void setUp() {
        loginService = new LoginServiceImpl(authenticationManager, jwtUtil);
    }

    @Test
    void loginShouldReturnSuccessWhenAuthenticationSucceeds() {
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getName()).thenReturn("customer@example.com");
        when(authentication.getAuthorities()).thenReturn(
                (java.util.Collection) List.of((GrantedAuthority) () -> "ROLE_CUSTOMER"));
        when(jwtUtil.createToken("customer@example.com", List.of("ROLE_CUSTOMER")))
                .thenReturn("jwt-token-value");

        LoginResultDto result = loginService.login("customer@example.com", "password123");

        assertThat(result.getResult()).isEqualTo("success");
        assertThat(result.getToken()).isEqualTo("jwt-token-value");
        assertThat(result.getUserDto()).isNotNull();
        assertThat(result.getUserDto().getEmail()).isEqualTo("customer@example.com");
    }
}
