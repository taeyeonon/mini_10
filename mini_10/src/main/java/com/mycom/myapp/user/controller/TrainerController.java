package com.mycom.myapp.user.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/trainer")
public class TrainerController {

	@GetMapping("/hello")
	public String hello() {
		return "Hello, Trainer";
	}
}
