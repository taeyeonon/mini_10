package com.mycom.myapp.payment.dto;

public record PaymentOrderResponse(
        String orderId, String orderName, long amount, String clientKey,
        String customerKey, String customerName, String customerEmail) {}
