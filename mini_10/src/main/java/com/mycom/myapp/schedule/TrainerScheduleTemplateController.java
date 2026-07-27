package com.mycom.myapp.schedule;

import com.mycom.myapp.schedule.dto.ScheduleTemplateCreateRequest;
import com.mycom.myapp.schedule.dto.ScheduleTemplateResponse;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trainer/schedule-templates")
public class TrainerScheduleTemplateController {

    private final TrainerScheduleTemplateService templateService;

    public TrainerScheduleTemplateController(TrainerScheduleTemplateService templateService) {
        this.templateService = templateService;
    }

    @PostMapping
    public ResponseEntity<ScheduleTemplateResponse> create(
            @AuthenticationPrincipal MyUserDetails userDetails,
            @Valid @RequestBody ScheduleTemplateCreateRequest request
    ) {
        ScheduleTemplateResponse response = templateService.create(userDetails.getEmail(), request);
        return ResponseEntity.created(
                URI.create("/api/trainer/schedule-templates/" + response.id())).body(response);
    }

    @GetMapping
    public List<ScheduleTemplateResponse> findMine(
            @AuthenticationPrincipal MyUserDetails userDetails
    ) {
        return templateService.findMine(userDetails.getEmail());
    }

    @PatchMapping("/{templateId}/deactivate")
    public ScheduleTemplateResponse deactivate(
            @AuthenticationPrincipal MyUserDetails userDetails,
            @PathVariable("templateId") Long templateId
    ) {
        return templateService.deactivate(userDetails.getEmail(), templateId);
    }

}
