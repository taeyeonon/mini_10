package com.mycom.myapp.ticket.service;

import java.util.List;
import java.time.LocalDate;

import com.mycom.myapp.ticket.dto.TicketCreateRequestDto;
import com.mycom.myapp.ticket.dto.TicketResponseDto;
import com.mycom.myapp.ticket.entity.Ticket;

public interface TicketService {
	Long issueTicket(TicketCreateRequestDto dto);
	Long issueTicket(Long userId, int totalCount, LocalDate startDate, LocalDate endDate);
	List<TicketResponseDto> getMyTickets(Long userId);
//	void useTicket(Long userId);
	Ticket useTicketAndGet(Long userId);
	void cancelTicket(Ticket ticket);
}
