package com.mycom.myapp.reservation.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReservationResultDto {
	private String result;    
	private String message;   
	private ReservationDto reservationDto;    
	private List<ReservationDto> reservationDtoList;  
}