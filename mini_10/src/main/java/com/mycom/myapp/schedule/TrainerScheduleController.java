package com.mycom.myapp.schedule;

import com.mycom.myapp.schedule.dto.ScheduleCreateRequest;
import com.mycom.myapp.schedule.dto.ScheduleRefreshResponse;
import com.mycom.myapp.schedule.dto.ScheduleResponse;
import com.mycom.myapp.schedule.dto.ScheduleUpdateRequest;
import com.mycom.myapp.config.MyUserDetails;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trainer/schedules")
public class TrainerScheduleController {

    private final TrainerScheduleService scheduleService;

    public TrainerScheduleController(TrainerScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @PostMapping
    public ResponseEntity<ScheduleResponse> create(
            @AuthenticationPrincipal MyUserDetails userDetails,
            @Valid @RequestBody ScheduleCreateRequest request
    ) {
        ScheduleResponse response = scheduleService.create(userDetails.getEmail(), request);
        return ResponseEntity.created(URI.create("/api/schedules/" + response.id())).body(response);
    }

    @GetMapping
    public List<ScheduleResponse> findMine(
            @AuthenticationPrincipal MyUserDetails userDetails
    ) {
        return scheduleService.findMine(userDetails.getEmail());
    }

    @PutMapping("/{scheduleId}")
    public ScheduleResponse update(
            @AuthenticationPrincipal MyUserDetails userDetails,
            @PathVariable Long scheduleId,
            @Valid @RequestBody ScheduleUpdateRequest request
    ) {
        return scheduleService.update(userDetails.getEmail(), scheduleId, request);
    }

    @PatchMapping("/{scheduleId}/cancel")
    public ScheduleResponse cancel(
            @AuthenticationPrincipal MyUserDetails userDetails,
            @PathVariable Long scheduleId
    ) {
        return scheduleService.cancel(userDetails.getEmail(), scheduleId);
    }

    @PatchMapping("/{scheduleId}/restore")
    public ScheduleResponse restore(
            @AuthenticationPrincipal MyUserDetails userDetails,
            @PathVariable Long scheduleId
    ) {
        return scheduleService.restore(userDetails.getEmail(), scheduleId);
    }

    @PostMapping("/refresh")
    public ScheduleRefreshResponse refresh(
            @AuthenticationPrincipal MyUserDetails userDetails
    ) {
        return scheduleService.refresh(userDetails.getEmail());
    }
}
