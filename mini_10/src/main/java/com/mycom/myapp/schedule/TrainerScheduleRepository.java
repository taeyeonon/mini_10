package com.mycom.myapp.schedule;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainerScheduleRepository extends JpaRepository<TrainerSchedule, Long> {

    List<TrainerSchedule> findAllByTrainerIdOrderByStartTimeAsc(Long trainerId);

    List<TrainerSchedule> findAllByStatusAndStartTimeAfterOrderByStartTimeAsc(
            ScheduleStatus status, LocalDateTime startTime
    );

    boolean existsByTrainerIdAndStartTimeLessThanAndEndTimeGreaterThan(
            Long trainerId, LocalDateTime endTime, LocalDateTime startTime
    );

    boolean existsByTrainerIdAndStartTimeLessThanAndEndTimeGreaterThanAndIdNot(
            Long trainerId, LocalDateTime endTime, LocalDateTime startTime, Long id
    );

    boolean existsByTrainerIdAndStartTime(Long trainerId, LocalDateTime startTime);
}
