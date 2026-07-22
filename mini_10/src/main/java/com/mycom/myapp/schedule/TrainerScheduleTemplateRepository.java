package com.mycom.myapp.schedule;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainerScheduleTemplateRepository
        extends JpaRepository<TrainerScheduleTemplate, Long> {

    List<TrainerScheduleTemplate> findAllByTrainerIdOrderByDayOfWeekAscStartTimeAsc(Long trainerId);

    List<TrainerScheduleTemplate> findAllByTrainerIdAndActiveTrue(Long trainerId);

    boolean existsByTrainerIdAndDayOfWeekAndStartTimeLessThanAndEndTimeGreaterThanAndActiveTrue(
            Long trainerId,
            java.time.DayOfWeek dayOfWeek,
            java.time.LocalTime endTime,
            java.time.LocalTime startTime
    );
}
