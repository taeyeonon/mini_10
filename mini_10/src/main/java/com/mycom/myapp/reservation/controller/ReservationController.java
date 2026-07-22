package com.mycom.myapp.reservation.controller;

import com.mycom.myapp.config.MyUserDetails;
import com.mycom.myapp.reservation.dto.ReservationResponse;
import com.mycom.myapp.reservation.service.ReservationService;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/customer/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/schedules/{scheduleId}")
    public ResponseEntity<ReservationResponse> reserve(
            @AuthenticationPrincipal MyUserDetails userDetails,
            @PathVariable Long scheduleId
    ) {
        ReservationResponse response = reservationService.reserve(userDetails.getId(), scheduleId);
        return ResponseEntity.created(
                URI.create("/customer/reservations/" + response.id())).body(response);
    }

    @GetMapping("/me")
    public List<ReservationResponse> findMine(
            @AuthenticationPrincipal MyUserDetails userDetails
    ) {
        return reservationService.findMine(userDetails.getId());
    }

    @PatchMapping("/{reservationId}/cancel")
    public ReservationResponse cancel(
            @AuthenticationPrincipal MyUserDetails userDetails,
            @PathVariable Long reservationId
    ) {
        return reservationService.cancel(userDetails.getId(), reservationId);
    }
}
