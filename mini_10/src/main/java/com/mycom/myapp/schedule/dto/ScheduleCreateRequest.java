package com.mycom.myapp.schedule.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record ScheduleCreateRequest(
        @NotNull(message = "수업 시작 시각은 필수입니다.")
        @Future(message = "수업 시작 시각은 현재보다 이후여야 합니다.")
        LocalDateTime startTime,

        @NotNull(message = "수업 종료 시각은 필수입니다.")
        LocalDateTime endTime,

        @Min(value = 1, message = "수업 정원은 1명 이상이어야 합니다.")
        int capacity
) {
}
