package com.mycom.myapp.reservation.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

public enum ReservationStatus {
	CONFIRMED,
	CANCELLED
}
