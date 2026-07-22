package com.mycom.myapp.ticket.dto;

import java.time.LocalDate;

import com.mycom.myapp.ticket.entity.Ticket;

import lombok.Getter;

@Getter
public class TicketResponseDto { // 응답 DTO
	private Long ticketId;
	private Integer totalCount;
	private Integer remainingCount;
	private LocalDate startDate;
	private LocalDate endDate;
	
	public TicketResponseDto(Ticket ticket) {
		this.ticketId = ticket.getId();
		this.totalCount = ticket.getTotalCount();
		this.remainingCount = ticket.getRemainingCount();
		this.startDate = ticket.getStartDate();
		this.endDate = ticket.getEndDate();
	}
	
	
}
