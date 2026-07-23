package com.mycom.myapp.schedule.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

public record ScheduleTemplateCreateRequest(
        @NotNull(message = "요일은 필수입니다.") DayOfWeek dayOfWeek,
        @NotNull(message = "수업 시작 시간은 필수입니다.") LocalTime startTime,
        @NotNull(message = "수업 종료 시간은 필수입니다.") LocalTime endTime,
        @Min(value = 1, message = "수업 정원은 1명 이상이어야 합니다.") int capacity,
        @NotNull(message = "적용 시작일은 필수입니다.") LocalDate effectiveFrom,
        LocalDate effectiveTo
) {
}
