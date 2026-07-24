package com.mycom.myapp.payment.dto;

import com.mycom.myapp.payment.PaymentOrder;
import com.mycom.myapp.payment.PaymentStatus;
import java.time.LocalDateTime;

/** 관리자 결제 내역 목록의 한 행. PaymentOrder 엔티티 필드를 그대로 노출한다. */
public record AdminPaymentResponse(
        Long id, Long userId, String userName, String userEmail,
        String orderId, int ticketCount, long amount, PaymentStatus status,
        String paymentKey, LocalDateTime createdAt, LocalDateTime approvedAt) {

    public static AdminPaymentResponse from(PaymentOrder order) {
        return new AdminPaymentResponse(
                order.getId(),
                order.getUser().getId(), order.getUser().getName(), order.getUser().getEmail(),
                order.getOrderId(), order.getTicketCount(), order.getAmount(), order.getStatus(),
                order.getPaymentKey(), order.getCreatedAt(), order.getApprovedAt());
    }
}
