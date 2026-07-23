package com.mycom.myapp.payment.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record PaymentOrderRequest(@Min(1) @Max(50) int ticketCount) {}
