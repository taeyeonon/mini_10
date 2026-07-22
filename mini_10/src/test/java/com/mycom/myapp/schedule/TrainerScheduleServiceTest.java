package com.mycom.myapp.schedule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mycom.myapp.common.ForbiddenOperationException;
import com.mycom.myapp.common.InvalidOperationException;
import com.mycom.myapp.user.entity.User;
import com.mycom.myapp.user.repository.UserRepository;
import com.mycom.myapp.user.entity.UserRole;
import com.mycom.myapp.schedule.dto.ScheduleCreateRequest;
import com.mycom.myapp.schedule.dto.ScheduleResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrainerScheduleServiceTest {

    @Mock
    private TrainerScheduleRepository scheduleRepository;

    @Mock
    private UserRepository memberRepository;

    private TrainerScheduleService scheduleService;

    @BeforeEach
    void setUp() {
        scheduleService = new TrainerScheduleService(scheduleRepository, memberRepository);
    }

    @Test
    void createScheduleForTrainer() {
        User trainer = trainer();
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusHours(1);
        when(memberRepository.findByEmail("trainer@example.com")).thenReturn(Optional.of(trainer));
        when(scheduleRepository.existsByTrainerIdAndStartTimeLessThanAndEndTimeGreaterThan(
                trainer.getId(), end, start)).thenReturn(false);
        when(scheduleRepository.save(any(TrainerSchedule.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ScheduleResponse response = scheduleService.create(
                " TRAINER@example.com ", new ScheduleCreateRequest(start, end, 10)
        );

        ArgumentCaptor<TrainerSchedule> captor = ArgumentCaptor.forClass(TrainerSchedule.class);
        verify(scheduleRepository).save(captor.capture());
        assertThat(captor.getValue().getTrainer()).isSameAs(trainer);
        assertThat(response.capacity()).isEqualTo(10);
        assertThat(response.status()).isEqualTo(ScheduleStatus.OPEN);
        assertThat(response.full()).isFalse();
        assertThat(response.availableCount()).isEqualTo(10);
    }

    @Test
    void createRejectsOverlappingSchedule() {
        User trainer = trainer();
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusHours(1);
        when(memberRepository.findByEmail("trainer@example.com")).thenReturn(Optional.of(trainer));
        when(scheduleRepository.existsByTrainerIdAndStartTimeLessThanAndEndTimeGreaterThan(
                trainer.getId(), end, start)).thenReturn(true);

        assertThatThrownBy(() -> scheduleService.create(
                "trainer@example.com", new ScheduleCreateRequest(start, end, 10)))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessage("같은 시간대에 이미 등록된 수업이 있습니다.");
    }

    @Test
    void createRejectsNonTrainer() {
        User member = user("member@example.com", "?뚯썝", "CUSTOMER");
        when(memberRepository.findByEmail("member@example.com")).thenReturn(Optional.of(member));

        assertThatThrownBy(() -> scheduleService.create(
                "member@example.com",
                new ScheduleCreateRequest(
                        LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(1).plusHours(1),
                        10)))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void createRejectsEndTimeBeforeStartTime() {
        User trainer = trainer();
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        when(memberRepository.findByEmail("trainer@example.com")).thenReturn(Optional.of(trainer));

        assertThatThrownBy(() -> scheduleService.create(
                "trainer@example.com", new ScheduleCreateRequest(start, start.minusHours(1), 10)))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessage("종료 시각은 시작 시각보다 이후여야 합니다.");
    }

    private User trainer() {
        return user("trainer@example.com", "?몃젅?대꼫", "TRAINER");
    }

    private User user(String email, String name, String roleName) {
        UserRole role = new UserRole();
        role.setName(roleName);
        return User.builder()
                .email(email)
                .password("encoded")
                .name(name)
                .userRoles(List.of(role))
                .build();
    }
}

