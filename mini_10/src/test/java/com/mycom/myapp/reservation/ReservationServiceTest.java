package com.mycom.myapp.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import com.mycom.myapp.common.InvalidOperationException;
import com.mycom.myapp.reservation.entity.Reservation;
import com.mycom.myapp.reservation.entity.ReservationStatus;
import com.mycom.myapp.reservation.repository.ReservationRepository;
import com.mycom.myapp.reservation.service.ReservationService;
import com.mycom.myapp.schedule.TrainerSchedule;
import com.mycom.myapp.schedule.TrainerScheduleRepository;
import com.mycom.myapp.ticket.entity.Ticket;
import com.mycom.myapp.ticket.service.TicketService;
import com.mycom.myapp.user.entity.User;
import com.mycom.myapp.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock ReservationRepository reservationRepository;
    @Mock TrainerScheduleRepository scheduleRepository;
    @Mock TicketService ticketService;
    @Mock UserRepository userRepository;

    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        reservationService = new ReservationService(
                reservationRepository, scheduleRepository, ticketService, userRepository);
    }

    @Test
    void reserveUsesTicketAndIncreasesReservedCount() {
        User member = user("member@example.com");
        TrainerSchedule schedule = schedule();
        Ticket ticket = ticket(member);
        stubReservation(member, schedule, ticket);
        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        reservationService.reserve(1L, 10L);

        assertThat(ticket.getRemainingCount()).isEqualTo(9);
        assertThat(schedule.getReservedCount()).isEqualTo(1);
    }

    @Test
    void reserveRejectsDuplicateReservation() {
        User member = user("member@example.com");
        TrainerSchedule schedule = schedule();
        Ticket ticket = ticket(member);
        Reservation existing = new Reservation(member, schedule, ticket);
        when(userRepository.findById(1L)).thenReturn(Optional.of(member));
        when(scheduleRepository.findById(10L)).thenReturn(Optional.of(schedule));
        when(reservationRepository.findByMemberIdAndTrainerScheduleId(1L, 10L))
                .thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> reservationService.reserve(1L, 10L))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessage("이미 예약한 수업입니다.");
    }

    @Test
    void cancelRestoresTicketAndReservedCount() {
        User member = user("member@example.com");
        TrainerSchedule schedule = schedule();
        Ticket ticket = ticket(member);
        ticket.use();
        schedule.increaseReservedCount();
        Reservation reservation = new Reservation(member, schedule, ticket);
        when(reservationRepository.findByIdAndMemberId(20L, 1L))
                .thenReturn(Optional.of(reservation));
        doAnswer(invocation -> {
            invocation.<Ticket>getArgument(0).cancel();
            return null;
        }).when(ticketService).cancelTicket(ticket);

        reservationService.cancel(1L, 20L);

        assertThat(ticket.getRemainingCount()).isEqualTo(10);
        assertThat(schedule.getReservedCount()).isZero();
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
    }

    private void stubReservation(User member, TrainerSchedule schedule, Ticket ticket) {
        when(userRepository.findById(1L)).thenReturn(Optional.of(member));
        when(scheduleRepository.findById(10L)).thenReturn(Optional.of(schedule));
        when(reservationRepository.findByMemberIdAndTrainerScheduleId(1L, 10L))
                .thenReturn(Optional.empty());
        when(ticketService.useTicketAndGet(1L)).thenAnswer(invocation -> {
            ticket.use();
            return ticket;
        });
    }

    private User user(String email) {
        return User.builder().id(1L).email(email).name("회원").password("encoded").build();
    }

    private TrainerSchedule schedule() {
        User trainer = User.builder().id(2L).email("trainer@example.com").build();
        return new TrainerSchedule(
                trainer, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(1), 10);
    }

    private Ticket ticket(User member) {
        return Ticket.builder()
                .id(30L)
                .user(member)
                .totalCount(10)
                .remainingCount(10)
                .startDate(LocalDate.now().minusDays(1))
                .endDate(LocalDate.now().plusDays(30))
                .build();
    }
}
