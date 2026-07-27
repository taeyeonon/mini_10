package com.mycom.myapp.user.dto;

/** 회원 관리 화면의 역할 일괄 변경 요청 한 건 (회원 id → 바꿀 역할). */
public record RoleUpdateRequest(Long id, String role) {
}
