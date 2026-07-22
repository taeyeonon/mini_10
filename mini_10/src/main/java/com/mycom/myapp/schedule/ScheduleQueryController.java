package com.mycom.myapp.schedule;

import com.mycom.myapp.schedule.dto.ScheduleResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleQueryController {

    private final TrainerScheduleService scheduleService;

    public ScheduleQueryController(TrainerScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping
    public List<ScheduleResponse> findAvailable() {
        return scheduleService.findAvailable();
    }

    @GetMapping("/{scheduleId}")
    public ScheduleResponse findOne(@PathVariable Long scheduleId) {
        return scheduleService.findOne(scheduleId);
    }
}
