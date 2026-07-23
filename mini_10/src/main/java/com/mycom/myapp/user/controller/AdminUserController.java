package com.mycom.myapp.user.controller;

import com.mycom.myapp.user.dto.AdminUserSummary;
import com.mycom.myapp.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserRepository userRepository;

    @GetMapping("/customers")
    public List<AdminUserSummary> customers() {
        return userRepository.findDistinctByUserRolesNameOrderByNameAsc("CUSTOMER")
                .stream().map(AdminUserSummary::from).toList();
    }
}
