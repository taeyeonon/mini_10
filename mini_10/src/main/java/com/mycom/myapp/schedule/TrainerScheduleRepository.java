package com.mycom.myapp.schedule;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainerScheduleRepository extends JpaRepository<TrainerSchedule, Long> {

    List<TrainerSchedule> findAllByTrainerIdAndArchivedFalseOrderByStartTimeAsc(Long trainerId);

    List<TrainerSchedule> findAllByTrainerIdAndStatusAndArchivedFalse(
            Long trainerId, ScheduleStatus status
    );

    List<TrainerSchedule> findAllByStatusAndStartTimeAfterOrderByStartTimeAsc(
            ScheduleStatus status, LocalDateTime startTime
    );

    boolean existsByTrainerIdAndStatusNotAndStartTimeLessThanAndEndTimeGreaterThan(
            Long trainerId, ScheduleStatus status,
            LocalDateTime endTime, LocalDateTime startTime
    );

    boolean existsByTrainerIdAndStatusNotAndStartTimeLessThanAndEndTimeGreaterThanAndIdNot(
            Long trainerId, ScheduleStatus status,
            LocalDateTime endTime, LocalDateTime startTime, Long id
    );

    boolean existsByTemplateIdAndStartTimeAndEndTime(
            Long templateId, LocalDateTime startTime, LocalDateTime endTime
    );
}
