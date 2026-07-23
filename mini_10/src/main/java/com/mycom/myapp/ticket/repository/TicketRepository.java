package com.mycom.myapp.ticket.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mycom.myapp.ticket.entity.Ticket;


public interface TicketRepository extends JpaRepository<Ticket, Long>{
	List<Ticket> findByUserId(Long userId);
	
	Optional<Ticket> findFirstByUserIdAndRemainingCountGreaterThanAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByIdAsc(
			Long userId, Integer remainingCount, LocalDate todayForStart, LocalDate todayForEnd);
}
