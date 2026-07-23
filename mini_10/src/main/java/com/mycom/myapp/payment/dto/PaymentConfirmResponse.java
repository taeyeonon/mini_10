package com.mycom.myapp.payment.dto;

public record PaymentConfirmResponse(String orderId, long amount, int ticketCount, Long ticketId) {}
