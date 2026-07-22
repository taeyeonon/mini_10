package com.mycom.myapp.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.mycom.myapp.user.entity.UserRole;
import com.mycom.myapp.user.repository.UserRoleRepository;

import lombok.RequiredArgsConstructor;

@Component				// Spring이 관리하는 Bean으로 등록 (자동 실행 대상이 됨)
@RequiredArgsConstructor
public class UserRoleInitializer implements ApplicationRunner{

	private final UserRoleRepository userRoleRepository;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		createRoleIfAbsent("CUSTOMER");
		createRoleIfAbsent("TRAINER");
		createRoleIfAbsent("ADMIN");
	}

	private void createRoleIfAbsent(String name) {
		if( userRoleRepository.findByName(name) == null ) {
			UserRole role = new UserRole();	// new 상태 (아직 영속화 X)
			role.setName(name);
			userRoleRepository.save(role);	// INSERT 실행 → 영속화됨
		}
	}
}
