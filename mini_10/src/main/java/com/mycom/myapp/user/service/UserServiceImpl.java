package com.mycom.myapp.user.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.mycom.myapp.common.InvalidOperationException;
import com.mycom.myapp.common.ResourceNotFoundException;
import com.mycom.myapp.user.dto.AdminUserSummary;
import com.mycom.myapp.user.dto.RoleUpdateRequest;
import com.mycom.myapp.user.dto.UserDto;
import com.mycom.myapp.user.dto.UserResultDto;
import com.mycom.myapp.user.entity.User;
import com.mycom.myapp.user.entity.UserRole;
import com.mycom.myapp.user.repository.UserRepository;
import com.mycom.myapp.user.repository.UserRoleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService{

	// 사용자 등록할 때 CUSTOMER Role 부여 
	
	private final UserRepository userRepository;
	private final UserRoleRepository userRoleRepository;
	
	// 사용자 입력 패스워드 (일반 텍스트) 암호화 후 저장
	// 내맘대로 암호화가 아니라 현재 프로젝트에 설정된 암호화 객체 이용 => DI
	private final PasswordEncoder passwordEncoder;
	
	@Override
	public UserResultDto insertUser(UserDto userDto) {
		UserResultDto userResultDto = new UserResultDto();
		
		try {
			List<String> requestedRoles = userDto.getRoles();
			if (requestedRoles == null || requestedRoles.isEmpty()) {
				requestedRoles = List.of("CUSTOMER");
			}

			List<UserRole> userRoles = requestedRoles.stream()
					.map(String::trim)
					.map(String::toUpperCase)
					.distinct()
					.map(roleName -> {
						UserRole userRole = userRoleRepository.findByName(roleName);
						if (userRole == null) {
							throw new IllegalArgumentException("지원하지 않는 역할입니다: " + roleName);
						}
						return userRole;
					})
					.toList();

			// 비밀번호 암호화 후 사용자 정보 저장
			User user = User.builder()
							.name(userDto.getName())
							.email(userDto.getEmail())
							.password(passwordEncoder.encode(userDto.getPassword()))
							.userRoles(userRoles)
							.build();
			User savedUser = userRepository.save(user);
			log.debug("User Registerd: {}", savedUser.getEmail());
			
			userResultDto.setResult("success");
		}catch(Exception e) {
			e.printStackTrace();
			// 트랜잭션 롤백 (사용자와 권한 정보 insert 실패 시)
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			userResultDto.setResult("fail");
		}
		
		return userResultDto;
	}

	// 변경 가능한 역할 (관리자 화면 드롭다운과 동일)
	private static final Set<String> ALLOWED_ROLES = Set.of("CUSTOMER", "TRAINER", "ADMIN");

	@Override
	@Transactional
	public List<AdminUserSummary> updateRoles(List<RoleUpdateRequest> requests, Long currentAdminId) {
		if (requests == null || requests.isEmpty()) {
			throw new InvalidOperationException("변경할 역할이 없습니다.");
		}

		for (RoleUpdateRequest request : requests) {
			String roleName = request.role() == null ? "" : request.role().trim().toUpperCase();
			if (!ALLOWED_ROLES.contains(roleName)) {
				throw new InvalidOperationException("지원하지 않는 역할입니다: " + request.role());
			}
			// 본인 계정의 관리자 권한을 스스로 내리면 관리 화면 접근이 막히므로 방지한다.
			if (request.id().equals(currentAdminId) && !"ADMIN".equals(roleName)) {
				throw new InvalidOperationException("본인 계정의 관리자 권한은 변경할 수 없습니다.");
			}

			User user = userRepository.findById(request.id())
					.orElseThrow(() -> new ResourceNotFoundException("회원을 찾을 수 없습니다: " + request.id()));
			UserRole role = userRoleRepository.findByName(roleName);
			if (role == null) {
				throw new InvalidOperationException("역할 정보를 찾을 수 없습니다: " + roleName);
			}

			// 회원당 하나의 역할만 갖도록 기존 역할을 교체한다.
			user.setUserRoles(new ArrayList<>(List.of(role)));
			userRepository.save(user);
			log.debug("Role updated: userId={} -> {}", user.getId(), roleName);
		}

		return userRepository.findAllByOrderByIdAsc()
				.stream().map(AdminUserSummary::from).toList();
	}

}










