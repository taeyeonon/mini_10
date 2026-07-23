package com.mycom.myapp.payment;

import com.mycom.myapp.config.MyUserDetails;
import com.mycom.myapp.payment.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customer/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/orders")
    public PaymentOrderResponse createOrder(
            @AuthenticationPrincipal MyUserDetails user,
            @Valid @RequestBody PaymentOrderRequest request) {
        return paymentService.createOrder(user.getId(), request);
    }

    @PostMapping("/confirm")
    public PaymentConfirmResponse confirm(
            @AuthenticationPrincipal MyUserDetails user,
            @Valid @RequestBody PaymentConfirmRequest request) {
        return paymentService.confirm(user.getId(), request);
    }
}
