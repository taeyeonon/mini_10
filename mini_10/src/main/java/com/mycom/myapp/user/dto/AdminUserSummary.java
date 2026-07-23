package com.mycom.myapp.user.dto;

import com.mycom.myapp.user.entity.User;

public record AdminUserSummary(Long id, String name, String email) {
    public static AdminUserSummary from(User user) {
        return new AdminUserSummary(user.getId(), user.getName(), user.getEmail());
    }
}
