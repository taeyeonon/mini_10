package com.mycom.myapp.ticket.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycom.myapp.ticket.dto.TicketCreateRequestDto;
import com.mycom.myapp.ticket.service.TicketService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/tickets")
@RequiredArgsConstructor
public class AdminTicketController {
	
	private final TicketService ticketService;
	
	// 수강권 발급
	@PostMapping("/issue")
	public ResponseEntity<String> issueTicket(@RequestBody TicketCreateRequestDto dto) {
		Long ticketId = ticketService.issueTicket(dto);
		return ResponseEntity.ok("수강권 발급 완료 (Ticket ID: " + ticketId + ")");
	}

}
