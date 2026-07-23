package com.mycom.myapp.reservation.entity;

import com.mycom.myapp.schedule.TrainerSchedule;
import com.mycom.myapp.ticket.entity.Ticket;
import com.mycom.myapp.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "reservation",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_reservation_member_schedule",
                columnNames = {"member_id", "trainer_schedule_id"}
        )
)
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private User member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trainer_schedule_id", nullable = false)
    private TrainerSchedule trainerSchedule;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @Column(nullable = false)
    private LocalDateTime reservedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status;

    protected Reservation() {
    }

    public Reservation(User member, TrainerSchedule trainerSchedule, Ticket ticket) {
        this.member = member;
        this.trainerSchedule = trainerSchedule;
        this.ticket = ticket;
        this.reservedAt = LocalDateTime.now();
        this.status = ReservationStatus.CONFIRMED;
    }

    public void cancel() {
        this.status = ReservationStatus.CANCELLED;
    }

    public void confirm(Ticket ticket) {
        this.ticket = ticket;
        this.reservedAt = LocalDateTime.now();
        this.status = ReservationStatus.CONFIRMED;
    }

    public Long getId() { return id; }
    public User getMember() { return member; }
    public TrainerSchedule getTrainerSchedule() { return trainerSchedule; }
    public Ticket getTicket() { return ticket; }
    public LocalDateTime getReservedAt() { return reservedAt; }
    public ReservationStatus getStatus() { return status; }
}
