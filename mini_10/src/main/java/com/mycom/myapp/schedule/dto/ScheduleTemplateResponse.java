package com.mycom.myapp.schedule.dto;

import com.mycom.myapp.schedule.TrainerScheduleTemplate;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

public record ScheduleTemplateResponse(
        Long id,
        Long trainerId,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        int capacity,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        boolean active
) {
    public static ScheduleTemplateResponse from(TrainerScheduleTemplate template) {
        return new ScheduleTemplateResponse(
                template.getId(), template.getTrainer().getId(), template.getDayOfWeek(),
                template.getStartTime(), template.getEndTime(), template.getCapacity(),
                template.getEffectiveFrom(), template.getEffectiveTo(), template.isActive()
        );
    }
}
