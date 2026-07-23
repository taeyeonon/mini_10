package com.mycom.myapp.auth.service;

import com.mycom.myapp.auth.dto.LoginResultDto;

public interface LoginService {
	LoginResultDto login(String email, String password);
}
