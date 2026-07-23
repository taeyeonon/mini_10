package com.mycom.myapp.schedule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mycom.myapp.schedule.dto.ScheduleTemplateCreateRequest;
import com.mycom.myapp.schedule.dto.ScheduleTemplateResponse;
import com.mycom.myapp.user.entity.User;
import com.mycom.myapp.user.entity.UserRole;
import com.mycom.myapp.user.repository.UserRepository;
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
    void firstTemplateCreatesEveryMatchingClassDate() {
        mockTrainerAndTemplateSave(trainer(1L, "trainer@example.com"));
        ScheduleTemplateResponse response = templateService.create(
                "trainer@example.com", request(
                        LocalTime.of(10, 0), LocalTime.of(11, 0),
                        LocalDate.of(2027, 1, 26), LocalDate.of(2027, 2, 24)));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<TrainerSchedule>> captor = ArgumentCaptor.forClass(List.class);
        verify(scheduleRepository).saveAll(captor.capture());
        assertThat(response.active()).isTrue();
        assertThat(captor.getValue()).hasSize(4);
        assertThat(captor.getValue()).extracting(TrainerSchedule::getStartTime)
                .extracting(java.time.LocalDateTime::toLocalDate)
                .containsExactly(
                        LocalDate.of(2027, 2, 1), LocalDate.of(2027, 2, 8),
                        LocalDate.of(2027, 2, 15), LocalDate.of(2027, 2, 22));
    }

    @Test
    void overlappingTemplateIsRejectedBeforeSave() {
        User trainer = trainer(1L, "trainer@example.com");
        when(memberRepository.findByEmail(trainer.getEmail())).thenReturn(Optional.of(trainer));
        when(templateRepository.countOverlappingActiveTemplates(
                trainer.getId(), DayOfWeek.MONDAY,
                LocalTime.of(10, 0), LocalTime.of(11, 0),
                LocalDate.of(2027, 1, 26), LocalDate.of(2027, 2, 24)))
                .thenReturn(1L);

        assertThatThrownBy(() -> templateService.create(trainer.getEmail(), request(
                LocalTime.of(10, 0), LocalTime.of(11, 0),
                LocalDate.of(2027, 1, 26), LocalDate.of(2027, 2, 24))))
                .isInstanceOf(DuplicateRecurringScheduleException.class)
                .hasMessage("같은 요일과 시간대에 이미 반복 일정이 있습니다.");
        verify(templateRepository, never()).save(any());
    }

    @Test
    void adjacentTimeRangeIsAccepted() {
        User trainer = trainer(1L, "trainer@example.com");
        mockTrainerAndTemplateSave(trainer);

        templateService.create(trainer.getEmail(), request(
                LocalTime.of(11, 0), LocalTime.of(12, 0),
                LocalDate.of(2027, 1, 26), LocalDate.of(2027, 2, 24)));

        verify(templateRepository).countOverlappingActiveTemplates(
                trainer.getId(), DayOfWeek.MONDAY,
                LocalTime.of(11, 0), LocalTime.of(12, 0),
                LocalDate.of(2027, 1, 26), LocalDate.of(2027, 2, 24));
        verify(templateRepository).save(any());
    }

    @Test
    void nonOverlappingPeriodIsAccepted() {
        User trainer = trainer(1L, "trainer@example.com");
        mockTrainerAndTemplateSave(trainer);

        templateService.create(trainer.getEmail(), request(
                LocalTime.of(10, 0), LocalTime.of(11, 0),
                LocalDate.of(2027, 3, 1), LocalDate.of(2027, 3, 31)));

        verify(templateRepository).countOverlappingActiveTemplates(
                trainer.getId(), DayOfWeek.MONDAY,
                LocalTime.of(10, 0), LocalTime.of(11, 0),
                LocalDate.of(2027, 3, 1), LocalDate.of(2027, 3, 31));
        verify(templateRepository).save(any());
    }

    @Test
    void sameSlotForAnotherTrainerIsAccepted() {
        User anotherTrainer = trainer(2L, "other@example.com");
        mockTrainerAndTemplateSave(anotherTrainer);

        templateService.create(anotherTrainer.getEmail(), request(
                LocalTime.of(10, 0), LocalTime.of(11, 0),
                LocalDate.of(2027, 1, 26), LocalDate.of(2027, 2, 24)));

        verify(templateRepository).countOverlappingActiveTemplates(
                anotherTrainer.getId(), DayOfWeek.MONDAY,
                LocalTime.of(10, 0), LocalTime.of(11, 0),
                LocalDate.of(2027, 1, 26), LocalDate.of(2027, 2, 24));
        verify(templateRepository).save(any());
    }

    @Test
    void createUsesOneMonthLaterWhenEndDateIsOmitted() {
        User trainer = trainer(1L, "trainer@example.com");
        mockTrainerAndTemplateSave(trainer);

        ScheduleTemplateResponse response = templateService.create(trainer.getEmail(), request(
                LocalTime.of(10, 0), LocalTime.of(11, 0),
                LocalDate.of(2027, 1, 31), null));

        assertThat(response.effectiveTo()).isEqualTo(LocalDate.of(2027, 2, 28));
    }

    private void mockTrainerAndTemplateSave(User trainer) {
        when(memberRepository.findByEmail(trainer.getEmail())).thenReturn(Optional.of(trainer));
        when(templateRepository.save(any(TrainerScheduleTemplate.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private ScheduleTemplateCreateRequest request(
            LocalTime startTime, LocalTime endTime,
            LocalDate effectiveFrom, LocalDate effectiveTo
    ) {
        return new ScheduleTemplateCreateRequest(
                DayOfWeek.MONDAY, startTime, endTime, 10, effectiveFrom, effectiveTo);
    }

    private User trainer(Long id, String email) {
        UserRole role = new UserRole();
        role.setName("TRAINER");
        return User.builder()
                .id(id)
                .email(email)
                .password("encoded")
                .name("트레이너")
                .userRoles(List.of(role))
                .build();
    }

}
