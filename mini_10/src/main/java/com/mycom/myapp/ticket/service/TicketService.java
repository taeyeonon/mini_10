package com.mycom.myapp.ticket.service;

import java.util.List;

import com.mycom.myapp.ticket.dto.TicketCreateRequestDto;
import com.mycom.myapp.ticket.dto.TicketResponseDto;

public interface TicketService {
	Long issueTicket(TicketCreateRequestDto dto);
	List<TicketResponseDto> getMyTickets(Long userId);
	void useTicket(Long userId);
}
