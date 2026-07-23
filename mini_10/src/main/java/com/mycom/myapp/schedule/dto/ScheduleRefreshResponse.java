package com.mycom.myapp.schedule.dto;

import java.util.List;

public record ScheduleRefreshResponse(
        int deletedCount,
        int archivedCount,
        List<ScheduleResponse> schedules
) {
}
