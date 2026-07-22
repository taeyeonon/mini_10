package com.mycom.myapp.ticket.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycom.myapp.config.MyUserDetails;
import com.mycom.myapp.ticket.dto.TicketCreateRequestDto;
import com.mycom.myapp.ticket.dto.TicketResponseDto;
import com.mycom.myapp.ticket.service.TicketService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/customer/tickets")
@RequiredArgsConstructor
public class CustomerTicketController {
	
	private final TicketService ticketService;
	
	// 수강권 조회 (내 수강권 목록 조회)
	@GetMapping("/me")
	public ResponseEntity<List<TicketResponseDto>> getMyTickets(@AuthenticationPrincipal MyUserDetails userDetails){
		Long loginUserId = userDetails.getId();
		List<TicketResponseDto> tickets = ticketService.getMyTickets(loginUserId);
		return ResponseEntity.ok(tickets);
	}
	
	// 수강권 차감/사용
	@PostMapping("/use")
	public ResponseEntity<String> useTicket(@AuthenticationPrincipal MyUserDetails userDetails){
		Long loginUserId = userDetails.getId();
		ticketService.useTicket(loginUserId);
		return ResponseEntity.ok("수강권 1회 차감 성공");
	}
}
