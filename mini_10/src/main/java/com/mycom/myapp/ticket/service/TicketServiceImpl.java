package com.mycom.myapp.ticket.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycom.myapp.ticket.dto.TicketCreateRequestDto;
import com.mycom.myapp.ticket.dto.TicketResponseDto;
import com.mycom.myapp.ticket.entity.Ticket;
import com.mycom.myapp.ticket.repository.TicketRepository;
import com.mycom.myapp.user.entity.User;
import com.mycom.myapp.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TicketServiceImpl implements TicketService{
	
	private final TicketRepository ticketRepository;
	private final UserRepository userRepository;
	
	// 수강권 발급
	@Transactional
	public Long issueTicket(TicketCreateRequestDto dto) {
		User user = userRepository.findById(dto.getUserId())
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다. ID: "+dto.getUserId()));
		Ticket ticket = Ticket.builder()
						.user(user)
						.totalCount(dto.getTotalCount())
						.remainingCount(dto.getTotalCount())
						.startDate(dto.getStartDate())
						.endDate(dto.getEndDate())
						.build();

		Ticket savedTicket = ticketRepository.save(ticket);
		
		log.debug("Ticket issued : {}", savedTicket.getId());
		
		
		return savedTicket.getId();
	}
	
	// 회원의 수강권 목록 조회
	public List<TicketResponseDto> getMyTickets(Long userId) {
		return ticketRepository.findByUserId(userId).stream()
				.map(TicketResponseDto::new)
				.collect(Collectors.toList());
	}
	
	// 수강권 차감
	@Transactional
	public Ticket useTicketAndGet(Long userId) {
		LocalDate today = LocalDate.now();
		
		Ticket ticket = ticketRepository.findFirstByUserIdAndRemainingCountGreaterThanAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByIdAsc(
				userId, 0, today, today
		).orElseThrow(() -> new IllegalStateException("사용 가능한 수강권이 없습니다."));
		
		ticket.use();
		return ticket;
	}

//	@Transactional
//	public void useTicket(Long userId) {
//		useTicketAndGet(userId);
//	}
	
	@Transactional
	public void cancelTicket(Ticket ticket) {
	    if (ticket == null) {
	        throw new IllegalArgumentException("복구할 수강권 정보가 없습니다.");
	    }
	    // Ticket 엔티티의 cancel() (복구) 메서드 호출
	    ticket.cancel();
	    
	    log.debug("Ticket restored. ID: {}, remainingCount: {}", ticket.getId(), ticket.getRemainingCount());
	}
}
