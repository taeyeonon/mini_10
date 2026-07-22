package com.mycom.myapp.reservation.dto;

import java.time.LocalDateTime;
import java.util.Date;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDto {
	private Long id;
	private Long trainerScheduleId;
	private String trainerName;
	private LocalDateTime stardTime;
	private LocalDateTime endTime;
	private Long ticketId;
	private Date reservedAt;
	private String status;
}
