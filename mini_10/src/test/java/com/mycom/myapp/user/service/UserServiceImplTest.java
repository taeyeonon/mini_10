package com.mycom.myapp.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mycom.myapp.user.dto.UserDto;
import com.mycom.myapp.user.dto.UserResultDto;
import com.mycom.myapp.user.entity.User;
import com.mycom.myapp.user.entity.UserRole;
import com.mycom.myapp.user.repository.UserRepository;
import com.mycom.myapp.user.repository.UserRoleRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, userRoleRepository, passwordEncoder);
    }

    @Test
    void insertUserShouldSaveUserWithEncodedPasswordAndCustomerRole() {
        UserDto userDto = UserDto.builder()
                .name("customer")
                .email("customer@example.com")
                .password("password123")
                .build();

        UserRole customerRole = new UserRole();
        customerRole.setName("CUSTOMER");

        when(userRoleRepository.findByName("CUSTOMER")).thenReturn(customerRole);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        UserResultDto result = userService.insertUser(userDto);

        assertThat(result.getResult()).isEqualTo("success");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }
}
