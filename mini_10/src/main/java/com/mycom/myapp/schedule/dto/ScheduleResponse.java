package com.mycom.myapp.schedule.dto;

import com.mycom.myapp.schedule.ScheduleStatus;
import com.mycom.myapp.schedule.TrainerSchedule;
import java.time.LocalDateTime;

public record ScheduleResponse(
        Long id,
        Long trainerId,
        String trainerName,
        LocalDateTime startTime,
        LocalDateTime endTime,
        int capacity,
        int reservedCount,
        int availableCount,
        boolean full,
        ScheduleStatus status
) {
    public static ScheduleResponse from(TrainerSchedule schedule) {
        return new ScheduleResponse(
                schedule.getId(),
                schedule.getTrainer().getId(),
                schedule.getTrainer().getName(),
                schedule.getStartTime(),
                schedule.getEndTime(),
                schedule.getCapacity(),
                schedule.getReservedCount(),
                schedule.getAvailableCount(),
                schedule.isFull(),
                schedule.getStatus()
        );
    }
}
