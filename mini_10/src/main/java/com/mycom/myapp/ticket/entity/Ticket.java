package com.mycom.myapp.ticket.entity;

import java.time.LocalDate;

import com.mycom.myapp.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false)
	private Integer totalCount;

	@Column(nullable = false)
	private Integer remainingCount;
	
	@Column(nullable = false)
	private LocalDate startDate;

	@Column(nullable = false)
	private LocalDate endDate;
	
	// 수강권 차감 로직
	public void use() {
		LocalDate today = LocalDate.now();
		
		if (today.isBefore(this.startDate)) {
			throw new IllegalStateException("수강권 기한 확인 필요");
		}
		if (today.isAfter(this.endDate)) {
			throw new IllegalStateException("만료된 수강권 입니다.");
		}
		if (this.remainingCount <= 0) {
			throw new IllegalStateException("남은 수강권 횟수가 없습니다.");
		}
		
		this.remainingCount--;
	}
	
	// 수강권 복구
	public void cancel() {
		if (this.remainingCount >= this.totalCount) {
			throw new IllegalStateException("초과 복구");
		}
		
		this.remainingCount++;
	}
	
}
