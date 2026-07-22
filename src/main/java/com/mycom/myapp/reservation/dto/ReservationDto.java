package com.mycom.myapp.reservation.dto;

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
	@Id
	private Long id;
	private Long trainerScheduleId;
	private Long ticketId;
	private Date reservedAt;
	private String status;
}
