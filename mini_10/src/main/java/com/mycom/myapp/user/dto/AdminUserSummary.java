package com.mycom.myapp.user.dto;

import com.mycom.myapp.user.entity.User;
import com.mycom.myapp.user.entity.UserRole;
import java.util.List;

public record AdminUserSummary(Long id, String name, String email, List<String> roles) {
    public static AdminUserSummary from(User user) {
        List<String> roles = user.getUserRoles() == null ? List.of()
                : user.getUserRoles().stream().map(UserRole::getName).toList();
        return new AdminUserSummary(user.getId(), user.getName(), user.getEmail(), roles);
    }
}
