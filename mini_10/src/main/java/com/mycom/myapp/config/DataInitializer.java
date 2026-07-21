package com.mycom.myapp.config;

import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.mycom.myapp.user.entity.User;
import com.mycom.myapp.user.entity.UserRole;
import com.mycom.myapp.user.repository.UserRepository;
import com.mycom.myapp.user.repository.UserRoleRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserRoleRepository userRoleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        createRoleIfAbsent("CUSTOMER");
        createRoleIfAbsent("TRAINER");
        createRoleIfAbsent("ADMIN");

        createUserIfAbsent("admin@test.com", "Admin User", "admin1234", "ADMIN");
        createUserIfAbsent("trainer@test.com", "Trainer User", "trainer1234", "TRAINER");
    }

    private void createRoleIfAbsent(String name) {
        if (userRoleRepository.findByName(name) == null) {
            UserRole role = new UserRole();
            role.setName(name);
            userRoleRepository.save(role);
        }
    }

    private void createUserIfAbsent(String email, String name, String password, String roleName) {
        if (userRepository.findByEmail(email).isPresent()) {
            return;
        }

        UserRole role = userRoleRepository.findByName(roleName);
        if (role == null) {
            createRoleIfAbsent(roleName);
            role = userRoleRepository.findByName(roleName);
        }

        User user = User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(password))
                .userRoles(List.of(role))
                .build();
        userRepository.save(user);
    }
}
