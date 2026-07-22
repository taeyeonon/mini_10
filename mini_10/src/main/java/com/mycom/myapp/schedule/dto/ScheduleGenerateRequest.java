package com.mycom.myapp.schedule.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ScheduleGenerateRequest(
        @NotNull(message = "생성 시작일은 필수입니다.") LocalDate startDate,
        @NotNull(message = "생성 종료일은 필수입니다.") LocalDate endDate
) {
}
