package com.mycom.myapp.user.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycom.myapp.config.MyUserDetails;
import com.mycom.myapp.user.dto.AdminUserSummary;
import com.mycom.myapp.user.dto.RoleUpdateRequest;
import com.mycom.myapp.user.repository.UserRepository;
import com.mycom.myapp.user.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserRepository userRepository;
    private final UserService userService;

    /** 회원 관리 화면용 전체 사용자 목록 (역할 포함). */
    @GetMapping
    public List<AdminUserSummary> users() {
        return userRepository.findAllByOrderByIdAsc()
                .stream().map(AdminUserSummary::from).toList();
    }

    @GetMapping("/customers")
    public List<AdminUserSummary> customers() {
        return userRepository.findDistinctByUserRolesNameOrderByNameAsc("CUSTOMER")
                .stream().map(AdminUserSummary::from).toList();
    }

    /** 여러 회원의 역할을 한 번에 변경하고 변경 후 전체 목록을 돌려준다. */
    @PutMapping("/roles")
    public List<AdminUserSummary> updateRoles(
            @RequestBody List<RoleUpdateRequest> requests,
            @AuthenticationPrincipal MyUserDetails userDetails) {
        return userService.updateRoles(requests, userDetails.getId());
    }
}
