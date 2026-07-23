package com.mycom.myapp.schedule;

import com.mycom.myapp.common.ForbiddenOperationException;
import com.mycom.myapp.common.InvalidOperationException;
import com.mycom.myapp.common.ResourceNotFoundException;
import com.mycom.myapp.user.entity.User;
import com.mycom.myapp.user.repository.UserRepository;
import com.mycom.myapp.schedule.dto.ScheduleCreateRequest;
import com.mycom.myapp.schedule.dto.ScheduleRefreshResponse;
import com.mycom.myapp.schedule.dto.ScheduleResponse;
import com.mycom.myapp.schedule.dto.ScheduleUpdateRequest;
import com.mycom.myapp.reservation.repository.ReservationRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TrainerScheduleService {

    private final TrainerScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;

    public TrainerScheduleService(
            TrainerScheduleRepository scheduleRepository,
            UserRepository userRepository,
            ReservationRepository reservationRepository
    ) {
        this.scheduleRepository = scheduleRepository;
        this.userRepository = userRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public ScheduleResponse create(String trainerEmail, ScheduleCreateRequest request) {
        User trainer = findTrainer(trainerEmail);
        validateTime(request.startTime(), request.endTime());
        ensureNoOverlap(trainer.getId(), null, request.startTime(), request.endTime());

        TrainerSchedule schedule = new TrainerSchedule(
                trainer, request.startTime(), request.endTime(), request.capacity()
        );
        return ScheduleResponse.from(scheduleRepository.save(schedule));
    }

    @Transactional(readOnly = true)
    public List<ScheduleResponse> findMine(String trainerEmail) {
        User trainer = findTrainer(trainerEmail);
        return scheduleRepository.findAllByTrainerIdAndArchivedFalseOrderByStartTimeAsc(trainer.getId()).stream()
                .map(ScheduleResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ScheduleResponse> findAvailable() {
        return scheduleRepository
                .findAllByStatusAndStartTimeAfterOrderByStartTimeAsc(ScheduleStatus.OPEN, LocalDateTime.now())
                .stream()
                .filter(schedule -> !schedule.isFull())
                .map(ScheduleResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ScheduleResponse findOne(Long scheduleId) {
        return ScheduleResponse.from(findSchedule(scheduleId));
    }

    @Transactional
    public ScheduleResponse update(String trainerEmail, Long scheduleId, ScheduleUpdateRequest request) {
        User trainer = findTrainer(trainerEmail);
        TrainerSchedule schedule = findOwnedSchedule(scheduleId, trainer.getId());
        if (schedule.getStatus() == ScheduleStatus.CANCELLED
                || schedule.getStatus() == ScheduleStatus.COMPLETED) {
            throw new InvalidOperationException("취소 또는 완료된 일정은 수정할 수 없습니다.");
        }
        if (request.capacity() < schedule.getReservedCount()) {
            throw new InvalidOperationException("정원을 현재 예약 인원보다 작게 변경할 수 없습니다.");
        }
        validateTime(request.startTime(), request.endTime());
        ensureNoOverlap(trainer.getId(), scheduleId, request.startTime(), request.endTime());
        schedule.update(request.startTime(), request.endTime(), request.capacity());
        return ScheduleResponse.from(schedule);
    }

    @Transactional
    public ScheduleResponse cancel(String trainerEmail, Long scheduleId) {
        User trainer = findTrainer(trainerEmail);
        TrainerSchedule schedule = findOwnedSchedule(scheduleId, trainer.getId());
        if (schedule.getStatus() == ScheduleStatus.COMPLETED) {
            throw new InvalidOperationException("완료된 일정은 취소할 수 없습니다.");
        }
        if (schedule.getStatus() == ScheduleStatus.CANCELLED) {
            throw new InvalidOperationException("이미 취소된 일정입니다.");
        }
        if (schedule.getReservedCount() > 0
                || reservationRepository.existsByTrainerScheduleId(schedule.getId())) {
            throw new InvalidOperationException(
                    "예약자가 있는 일정은 수강권 복구 정책이 필요하므로 취소할 수 없습니다.");
        }
        schedule.cancel();
        return ScheduleResponse.from(schedule);
    }

    @Transactional
    public ScheduleResponse restore(String trainerEmail, Long scheduleId) {
        User trainer = findTrainer(trainerEmail);
        TrainerSchedule schedule = findOwnedSchedule(scheduleId, trainer.getId());
        if (schedule.getStatus() != ScheduleStatus.CANCELLED) {
            throw new InvalidOperationException("취소된 일정만 복원할 수 있습니다.");
        }
        ensureNoOverlap(
                trainer.getId(), scheduleId, schedule.getStartTime(), schedule.getEndTime());
        schedule.restore();
        return ScheduleResponse.from(schedule);
    }

    @Transactional
    public ScheduleRefreshResponse refresh(String trainerEmail) {
        User trainer = findTrainer(trainerEmail);
        List<TrainerSchedule> cancelled = scheduleRepository
                .findAllByTrainerIdAndStatusAndArchivedFalse(
                        trainer.getId(), ScheduleStatus.CANCELLED);
        int deletedCount = 0;
        int archivedCount = 0;

        for (TrainerSchedule schedule : cancelled) {
            boolean hasReservationHistory = schedule.getReservedCount() > 0
                    || reservationRepository.existsByTrainerScheduleId(schedule.getId());
            if (hasReservationHistory) {
                schedule.archive();
                archivedCount++;
            } else {
                scheduleRepository.delete(schedule);
                deletedCount++;
            }
        }

        List<ScheduleResponse> schedules = scheduleRepository
                .findAllByTrainerIdAndArchivedFalseOrderByStartTimeAsc(trainer.getId()).stream()
                .map(ScheduleResponse::from)
                .toList();
        return new ScheduleRefreshResponse(deletedCount, archivedCount, schedules);
    }

    private User findTrainer(String email) {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("로그인한 사용자를 찾을 수 없습니다."));
        boolean isTrainer = user.getUserRoles().stream()
                .anyMatch(role -> "TRAINER".equals(role.getName()));
        if (!isTrainer) {
            throw new ForbiddenOperationException("트레이너만 수업 일정을 관리할 수 있습니다.");
        }
        return user;
    }

    private TrainerSchedule findSchedule(Long scheduleId) {
        return scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("수업 일정을 찾을 수 없습니다."));
    }

    private TrainerSchedule findOwnedSchedule(Long scheduleId, Long trainerId) {
        TrainerSchedule schedule = findSchedule(scheduleId);
        if (!schedule.getTrainer().getId().equals(trainerId)) {
            throw new ForbiddenOperationException("본인이 등록한 일정만 변경할 수 있습니다.");
        }
        return schedule;
    }

    private void validateTime(LocalDateTime startTime, LocalDateTime endTime) {
        if (!startTime.isBefore(endTime)) {
            throw new InvalidOperationException("종료 시각은 시작 시각보다 이후여야 합니다.");
        }
    }

    private void ensureNoOverlap(
            Long trainerId, Long scheduleId, LocalDateTime startTime, LocalDateTime endTime
    ) {
        boolean overlaps = scheduleId == null
                ? scheduleRepository
                        .existsByTrainerIdAndStatusNotAndStartTimeLessThanAndEndTimeGreaterThan(
                                trainerId, ScheduleStatus.CANCELLED, endTime, startTime)
                : scheduleRepository
                        .existsByTrainerIdAndStatusNotAndStartTimeLessThanAndEndTimeGreaterThanAndIdNot(
                                trainerId, ScheduleStatus.CANCELLED, endTime, startTime, scheduleId);
        if (overlaps) {
            throw new InvalidOperationException("같은 시간대에 이미 등록된 수업이 있습니다.");
        }
    }
}
