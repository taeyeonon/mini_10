package com.mycom.myapp.schedule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mycom.myapp.common.InvalidOperationException;
import com.mycom.myapp.user.entity.User;
import com.mycom.myapp.user.repository.UserRepository;
import com.mycom.myapp.user.entity.UserRole;
import com.mycom.myapp.schedule.dto.ScheduleGenerateRequest;
import com.mycom.myapp.schedule.dto.ScheduleGenerationResponse;
import com.mycom.myapp.schedule.dto.ScheduleTemplateCreateRequest;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrainerScheduleTemplateServiceTest {

    @Mock
    private TrainerScheduleTemplateRepository templateRepository;

    @Mock
    private TrainerScheduleRepository scheduleRepository;

    @Mock
    private UserRepository memberRepository;

    private TrainerScheduleTemplateService templateService;

    @BeforeEach
    void setUp() {
        templateService = new TrainerScheduleTemplateService(
                templateRepository, scheduleRepository, memberRepository);
    }

    @Test
    void createRejectsOverlappingTemplate() {
        User trainer = trainer();
        when(memberRepository.findByEmail("trainer@example.com")).thenReturn(Optional.of(trainer));
        when(templateRepository
                .existsByTrainerIdAndDayOfWeekAndStartTimeLessThanAndEndTimeGreaterThanAndActiveTrue(
                        trainer.getId(), DayOfWeek.MONDAY,
                        LocalTime.of(11, 0), LocalTime.of(10, 0)))
                .thenReturn(true);

        ScheduleTemplateCreateRequest request = new ScheduleTemplateCreateRequest(
                DayOfWeek.MONDAY,
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                10,
                LocalDate.of(2026, 7, 1),
                null
        );

        assertThatThrownBy(() -> templateService.create("trainer@example.com", request))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessage("媛숈? ?붿씪怨??쒓컙????대? 諛섎났 ?쇱젙???덉뒿?덈떎.");
    }

    @Test
    void generateCreatesEveryMatchingWeekday() {
        User trainer = trainer();
        TrainerScheduleTemplate template = mondayTemplate(trainer);
        when(memberRepository.findByEmail("trainer@example.com")).thenReturn(Optional.of(trainer));
        when(templateRepository.findAllByTrainerIdAndActiveTrue(trainer.getId()))
                .thenReturn(List.of(template));
        when(scheduleRepository
                .existsByTrainerIdAndStartTimeLessThanAndEndTimeGreaterThan(any(), any(), any()))
                .thenReturn(false);

        ScheduleGenerationResponse response = templateService.generate(
                "trainer@example.com",
                new ScheduleGenerateRequest(
                        LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31))
        );

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<TrainerSchedule>> captor = ArgumentCaptor.forClass(List.class);
        verify(scheduleRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(4);
        assertThat(response.createdCount()).isEqualTo(4);
        assertThat(response.skippedCount()).isZero();
    }

    @Test
    void generateSkipsExistingSchedule() {
        User trainer = trainer();
        TrainerScheduleTemplate template = mondayTemplate(trainer);
        when(memberRepository.findByEmail("trainer@example.com")).thenReturn(Optional.of(trainer));
        when(templateRepository.findAllByTrainerIdAndActiveTrue(trainer.getId()))
                .thenReturn(List.of(template));
        when(scheduleRepository
                .existsByTrainerIdAndStartTimeLessThanAndEndTimeGreaterThan(any(), any(), any()))
                .thenReturn(true);

        ScheduleGenerationResponse response = templateService.generate(
                "trainer@example.com",
                new ScheduleGenerateRequest(
                        LocalDate.of(2026, 7, 6), LocalDate.of(2026, 7, 6))
        );

        assertThat(response.createdCount()).isZero();
        assertThat(response.skippedCount()).isEqualTo(1);
    }

    private User trainer() {
        UserRole role = new UserRole();
        role.setName("TRAINER");
        return User.builder()
                .email("trainer@example.com")
                .password("encoded")
                .name("트레이너")
                .userRoles(List.of(role))
                .build();
    }

    private TrainerScheduleTemplate mondayTemplate(User trainer) {
        return new TrainerScheduleTemplate(
                trainer,
                DayOfWeek.MONDAY,
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                10,
                LocalDate.of(2026, 7, 1),
                null
        );
    }
}

