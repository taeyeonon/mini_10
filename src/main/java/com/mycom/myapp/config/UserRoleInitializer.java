package com.mycom.myapp.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.mycom.myapp.user.entity.UserRole;
import com.mycom.myapp.user.repository.UserRoleRepository;

import lombok.RequiredArgsConstructor;

/**
 * ═══════════════════════════════════════════════════════════════
 * UserRoleInitializer (권한 마스터 데이터 자동 등록)
 * ═══════════════════════════════════════════════════════════════
 *
 * [이 클래스가 필요한 이유]
 * - 회원가입 시 userRoleRepository.findByName("MEMBER")로 권한을 찾는데,
 *   user_role 테이블이 비어 있으면 null이 나와서 가입이 실패함!
 * - 그래서 앱이 시작할 때 3가지 권한(MEMBER, TRAINER, ADMIN)이
 *   없으면 자동으로 넣어줌
 *
 * [ApplicationRunner란?]
 * - Spring 컨텍스트 준비가 완료된 "직후" run() 메서드를 1번 실행해줌
 * - 초기 데이터 세팅에 딱 맞는 도구
 *
 * [실행 후 user_role 테이블 상태]
 *   id | name
 *   ───┼─────────
 *   1  | MEMBER
 *   2  | TRAINER
 *   3  | ADMIN
 */
@Component				// Spring이 관리하는 Bean으로 등록 (자동 실행 대상이 됨)
@RequiredArgsConstructor
public class UserRoleInitializer implements ApplicationRunner{

	private final UserRoleRepository userRoleRepository;

	/**
	 * 앱 시작 직후 1번 실행됨
	 * - 테이블 정의서 기준 3가지 권한: MEMBER, TRAINER, ADMIN
	 * - 이미 있으면 건너뛰므로 앱을 여러 번 재시작해도 중복 등록 안 됨
	 */
	@Override
	public void run(ApplicationArguments args) throws Exception {
		createRoleIfAbsent("MEMBER");
		createRoleIfAbsent("TRAINER");
		createRoleIfAbsent("ADMIN");
	}

	/**
	 * 해당 이름의 권한이 테이블에 없으면 추가
	 *
	 * @param name 권한 이름 (예: "MEMBER")
	 */
	private void createRoleIfAbsent(String name) {
		if( userRoleRepository.findByName(name) == null ) {
			UserRole role = new UserRole();	// new 상태 (아직 영속화 X)
			role.setName(name);
			userRoleRepository.save(role);	// INSERT 실행 → 영속화됨
		}
	}
}
