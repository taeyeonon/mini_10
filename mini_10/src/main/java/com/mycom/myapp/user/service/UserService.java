package com.mycom.myapp.user.service;

import java.util.List;

import com.mycom.myapp.user.dto.AdminUserSummary;
import com.mycom.myapp.user.dto.RoleUpdateRequest;
import com.mycom.myapp.user.dto.UserDto;
import com.mycom.myapp.user.dto.UserResultDto;

public interface UserService {
	UserResultDto insertUser(UserDto userDto);

	/**
	 * 관리자 화면에서 여러 회원의 역할을 한 번에 변경한다.
	 * @param requests    변경할 (회원 id, 역할) 목록
	 * @param currentAdminId 현재 로그인한 관리자 id (본인 권한 강등 방지용)
	 * @return 변경 후 전체 사용자 목록
	 */
	List<AdminUserSummary> updateRoles(List<RoleUpdateRequest> requests, Long currentAdminId);
}
