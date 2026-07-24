package com.mycom.myapp.schedule;

import com.mycom.myapp.schedule.dto.ScheduleResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/schedules")
public class AdminScheduleController {

    private final TrainerScheduleService scheduleService;

    public AdminScheduleController(TrainerScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping
    public List<ScheduleResponse> findAll() {
        return scheduleService.findAllForAdmin();
    }
}
