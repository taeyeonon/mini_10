package com.mycom.myapp.schedule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mycom.myapp.common.ForbiddenOperationException;
import com.mycom.myapp.common.InvalidOperationException;
import com.mycom.myapp.reservation.repository.ReservationRepository;
import com.mycom.myapp.schedule.dto.ScheduleCreateRequest;
import com.mycom.myapp.schedule.dto.ScheduleRefreshResponse;
import com.mycom.myapp.schedule.dto.ScheduleResponse;
import com.mycom.myapp.user.entity.User;
import com.mycom.myapp.user.entity.UserRole;
import com.mycom.myapp.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TrainerScheduleServiceTest {

    @Mock TrainerScheduleRepository scheduleRepository;
    @Mock UserRepository userRepository;
    @Mock ReservationRepository reservationRepository;

    private TrainerScheduleService service;

    @BeforeEach
    void setUp() {
        service = new TrainerScheduleService(
                scheduleRepository, userRepository, reservationRepository);
    }

    @Test
    void createScheduleForTrainer() {
        User trainer = trainer(1L, "trainer@example.com");
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusHours(1);
        when(userRepository.findByEmail(trainer.getEmail())).thenReturn(Optional.of(trainer));
        when(scheduleRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ScheduleResponse response = service.create(
                trainer.getEmail(), new ScheduleCreateRequest(start, end, 10));

        assertThat(response.status()).isEqualTo(ScheduleStatus.OPEN);
        assertThat(response.availableCount()).isEqualTo(10);
    }

    @Test
    void createRejectsOverlappingSchedule() {
        User trainer = trainer(1L, "trainer@example.com");
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusHours(1);
        when(userRepository.findByEmail(trainer.getEmail())).thenReturn(Optional.of(trainer));
        when(scheduleRepository
                .existsByTrainerIdAndStatusNotAndStartTimeLessThanAndEndTimeGreaterThan(
                        trainer.getId(), ScheduleStatus.CANCELLED, end, start)).thenReturn(true);

        assertThatThrownBy(() -> service.create(
                trainer.getEmail(), new ScheduleCreateRequest(start, end, 10)))
                .isInstanceOf(InvalidOperationException.class);
    }

    @Test
    void cancelAndRestoreSchedule() {
        User trainer = trainer(1L, "trainer@example.com");
        TrainerSchedule schedule = schedule(trainer);
        mockOwnedSchedule(trainer, schedule);

        assertThat(service.cancel(trainer.getEmail(), schedule.getId()).status())
                .isEqualTo(ScheduleStatus.CANCELLED);
        assertThat(service.restore(trainer.getEmail(), schedule.getId()).status())
                .isEqualTo(ScheduleStatus.OPEN);
    }

    @Test
    void restoreRejectsTimeConflict() {
        User trainer = trainer(1L, "trainer@example.com");
        TrainerSchedule schedule = schedule(trainer);
        schedule.cancel();
        mockOwnedSchedule(trainer, schedule);
        when(scheduleRepository
                .existsByTrainerIdAndStatusNotAndStartTimeLessThanAndEndTimeGreaterThanAndIdNot(
                        trainer.getId(), ScheduleStatus.CANCELLED,
                        schedule.getEndTime(), schedule.getStartTime(), schedule.getId()))
                .thenReturn(true);

        assertThatThrownBy(() -> service.restore(trainer.getEmail(), schedule.getId()))
                .isInstanceOf(InvalidOperationException.class);
    }

    @Test
    void anotherTrainerCannotCancelSchedule() {
        User current = trainer(1L, "trainer@example.com");
        TrainerSchedule schedule = schedule(trainer(2L, "owner@example.com"));
        when(userRepository.findByEmail(current.getEmail())).thenReturn(Optional.of(current));
        when(scheduleRepository.findById(schedule.getId())).thenReturn(Optional.of(schedule));

        assertThatThrownBy(() -> service.cancel(current.getEmail(), schedule.getId()))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void scheduleWithReservationHistoryCannotBeCancelled() {
        User trainer = trainer(1L, "trainer@example.com");
        TrainerSchedule schedule = schedule(trainer);
        mockOwnedSchedule(trainer, schedule);
        when(reservationRepository.existsByTrainerScheduleId(schedule.getId())).thenReturn(true);

        assertThatThrownBy(() -> service.cancel(trainer.getEmail(), schedule.getId()))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("수강권 복구 정책");
    }

    @Test
    void refreshDeletesCancelledScheduleWithoutReservationHistory() {
        User trainer = trainer(1L, "trainer@example.com");
        TrainerSchedule schedule = schedule(trainer);
        schedule.cancel();
        mockRefresh(trainer, schedule);

        ScheduleRefreshResponse response = service.refresh(trainer.getEmail());

        assertThat(response.deletedCount()).isEqualTo(1);
        assertThat(response.archivedCount()).isZero();
        verify(scheduleRepository).delete(schedule);
    }

    @Test
    void refreshArchivesCancelledScheduleWithReservationHistory() {
        User trainer = trainer(1L, "trainer@example.com");
        TrainerSchedule schedule = schedule(trainer);
        schedule.cancel();
        mockRefresh(trainer, schedule);
        when(reservationRepository.existsByTrainerScheduleId(schedule.getId())).thenReturn(true);

        ScheduleRefreshResponse response = service.refresh(trainer.getEmail());

        assertThat(response.deletedCount()).isZero();
        assertThat(response.archivedCount()).isEqualTo(1);
        assertThat(schedule.isArchived()).isTrue();
    }

    private void mockOwnedSchedule(User trainer, TrainerSchedule schedule) {
        when(userRepository.findByEmail(trainer.getEmail())).thenReturn(Optional.of(trainer));
        when(scheduleRepository.findById(schedule.getId())).thenReturn(Optional.of(schedule));
    }

    private void mockRefresh(User trainer, TrainerSchedule schedule) {
        when(userRepository.findByEmail(trainer.getEmail())).thenReturn(Optional.of(trainer));
        when(scheduleRepository.findAllByTrainerIdAndStatusAndArchivedFalse(
                trainer.getId(), ScheduleStatus.CANCELLED)).thenReturn(List.of(schedule));
        when(scheduleRepository.findAllByTrainerIdAndArchivedFalseOrderByStartTimeAsc(trainer.getId()))
                .thenReturn(List.of());
    }

    private TrainerSchedule schedule(User trainer) {
        LocalDateTime start = LocalDateTime.of(2027, 1, 26, 10, 0);
        TrainerSchedule schedule = new TrainerSchedule(trainer, start, start.plusHours(1), 10);
        ReflectionTestUtils.setField(schedule, "id", 11L);
        return schedule;
    }

    private User trainer(Long id, String email) {
        UserRole role = new UserRole();
        role.setName("TRAINER");
        return User.builder().id(id).email(email).password("encoded").name("트레이너")
                .userRoles(List.of(role)).build();
    }
}
