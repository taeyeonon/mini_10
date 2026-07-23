package com.mycom.myapp.schedule;

import com.mycom.myapp.common.ForbiddenOperationException;
import com.mycom.myapp.common.InvalidOperationException;
import com.mycom.myapp.common.ResourceNotFoundException;
import com.mycom.myapp.schedule.dto.ScheduleTemplateCreateRequest;
import com.mycom.myapp.schedule.dto.ScheduleTemplateResponse;
import com.mycom.myapp.user.entity.User;
import com.mycom.myapp.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TrainerScheduleTemplateService {

    private final TrainerScheduleTemplateRepository templateRepository;
    private final TrainerScheduleRepository scheduleRepository;
    private final UserRepository userRepository;

    public TrainerScheduleTemplateService(
            TrainerScheduleTemplateRepository templateRepository,
            TrainerScheduleRepository scheduleRepository,
            UserRepository userRepository
    ) {
        this.templateRepository = templateRepository;
        this.scheduleRepository = scheduleRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ScheduleTemplateResponse create(String trainerEmail, ScheduleTemplateCreateRequest request) {
        User trainer = findTrainer(trainerEmail);
        validateTemplate(request);
        LocalDate effectiveTo = request.effectiveTo() != null
                ? request.effectiveTo()
                : request.effectiveFrom().plusMonths(1);

        long overlapCount = templateRepository.countOverlappingActiveTemplates(
                trainer.getId(), request.dayOfWeek(), request.startTime(), request.endTime(),
                request.effectiveFrom(), effectiveTo);
        if (overlapCount > 0) {
            throw new DuplicateRecurringScheduleException(
                    "같은 요일과 시간대에 이미 반복 일정이 있습니다.");
        }

        TrainerScheduleTemplate template = new TrainerScheduleTemplate(
                trainer, request.dayOfWeek(), request.startTime(), request.endTime(),
                request.capacity(), request.effectiveFrom(), effectiveTo
        );
        TrainerScheduleTemplate savedTemplate = templateRepository.save(template);

        List<TrainerSchedule> schedules = buildSchedules(
                trainer, savedTemplate, request.effectiveFrom(), effectiveTo, true);
        scheduleRepository.saveAll(schedules);
        return ScheduleTemplateResponse.from(savedTemplate);
    }

    @Transactional(readOnly = true)
    public List<ScheduleTemplateResponse> findMine(String trainerEmail) {
        User trainer = findTrainer(trainerEmail);
        return templateRepository.findAllByTrainerIdOrderByDayOfWeekAscStartTimeAsc(trainer.getId())
                .stream()
                .map(ScheduleTemplateResponse::from)
                .toList();
    }

    @Transactional
    public ScheduleTemplateResponse deactivate(String trainerEmail, Long templateId) {
        User trainer = findTrainer(trainerEmail);
        TrainerScheduleTemplate template = findOwnedTemplate(templateId, trainer.getId());
        template.deactivate();
        return ScheduleTemplateResponse.from(template);
    }

    private List<TrainerSchedule> buildSchedules(
            User trainer,
            TrainerScheduleTemplate template,
            LocalDate startDate,
            LocalDate endDate,
            boolean failOnConflict
    ) {
        List<TrainerSchedule> schedules = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            if (!template.appliesTo(date)) {
                continue;
            }
            LocalDateTime startAt = LocalDateTime.of(date, template.getStartTime());
            LocalDateTime endAt = LocalDateTime.of(date, template.getEndTime());
            boolean exactDuplicate = template.getId() != null && scheduleRepository
                    .existsByTemplateIdAndStartTimeAndEndTime(template.getId(), startAt, endAt);
            boolean timeConflict = scheduleRepository
                    .existsByTrainerIdAndStatusNotAndStartTimeLessThanAndEndTimeGreaterThan(
                            trainer.getId(), ScheduleStatus.CANCELLED, endAt, startAt);

            if (exactDuplicate || timeConflict) {
                if (failOnConflict) {
                    throw new ScheduleConflictException(
                            startAt.toLocalDate() + " " + template.getStartTime() + "~"
                                    + template.getEndTime() + "에 겹치는 수업 일정이 있습니다.");
                }
                continue;
            }
            schedules.add(new TrainerSchedule(
                    trainer, template, startAt, endAt, template.getCapacity()));
        }
        return schedules;
    }

    private void validateTemplate(ScheduleTemplateCreateRequest request) {
        if (!request.startTime().isBefore(request.endTime())) {
            throw new InvalidOperationException("종료 시간은 시작 시간보다 이후여야 합니다.");
        }
        if (request.effectiveTo() != null && request.effectiveTo().isBefore(request.effectiveFrom())) {
            throw new InvalidOperationException("적용 종료일은 시작일보다 빠를 수 없습니다.");
        }
    }

    private User findTrainer(String email) {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("로그인한 사용자를 찾을 수 없습니다."));
        boolean isTrainer = user.getUserRoles().stream()
                .anyMatch(role -> "TRAINER".equals(role.getName()));
        if (!isTrainer) {
            throw new ForbiddenOperationException("트레이너만 반복 일정을 관리할 수 있습니다.");
        }
        return user;
    }

    private TrainerScheduleTemplate findOwnedTemplate(Long templateId, Long trainerId) {
        TrainerScheduleTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("반복 일정 템플릿을 찾을 수 없습니다."));
        if (!template.getTrainer().getId().equals(trainerId)) {
            throw new ForbiddenOperationException("본인의 반복 일정만 변경할 수 있습니다.");
        }
        return template;
    }
}
