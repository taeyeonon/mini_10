package com.mycom.myapp.reservation.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mycom.myapp.reservation.entity.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long>{
	
	Optional<Reservation> findByTrainerScheduleId(Long trainerScheduleId);
	
	Optional<Reservation> findByCustomerIdAndTrainerScheduleId(Long customerId, Long trainerScheduleId);
	
	List<Reservation> findByCustomerIdIdOrderByReservedAtDesc(Long customerId);
}
