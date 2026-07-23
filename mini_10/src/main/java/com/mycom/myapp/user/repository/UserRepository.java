package com.mycom.myapp.user.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mycom.myapp.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long>{

	// 이메일로 로그인 처리
	Optional<User> findByEmail(String email);

	List<User> findDistinctByUserRolesNameOrderByNameAsc(String roleName);
	
	// -[MIN]0720 로그인 뭘로 할지 확인
}
