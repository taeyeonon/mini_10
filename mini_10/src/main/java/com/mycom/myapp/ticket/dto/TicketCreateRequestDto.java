package com.mycom.myapp.ticket.dto;

import java.time.LocalDate;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TicketCreateRequestDto { // 발급 요청 DTO
	private Long userId;
	private Integer totalCount;
	private LocalDate startDate;
	private LocalDate endDate;
}
