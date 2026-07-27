package com.mycom.myapp.payment;

import com.mycom.myapp.user.entity.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_order", uniqueConstraints = {
        @UniqueConstraint(name = "uk_payment_order_id", columnNames = "order_id"),
        @UniqueConstraint(name = "uk_payment_key", columnNames = "payment_key")
})
public class PaymentOrder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(name = "order_id", nullable = false, length = 64)
    private String orderId;
    @Column(nullable = false)
    private int ticketCount;
    @Column(nullable = false)
    private long amount;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private PaymentStatus status;
    @Column(name = "payment_key", length = 200)
    private String paymentKey;
    @Column(nullable = false)
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;

    protected PaymentOrder() {}

    public PaymentOrder(User user, String orderId, int ticketCount, long amount) {
        this.user = user;
        this.orderId = orderId;
        this.ticketCount = ticketCount;
        this.amount = amount;
        this.status = PaymentStatus.READY;
        this.createdAt = LocalDateTime.now();
    }

    public void approve(String paymentKey) {
        if (status != PaymentStatus.READY) throw new IllegalStateException("이미 처리된 주문입니다.");
        this.paymentKey = paymentKey;
        this.status = PaymentStatus.PAID;
        this.approvedAt = LocalDateTime.now();
    }
    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getOrderId() { return orderId; }
    public int getTicketCount() { return ticketCount; }
    public long getAmount() { return amount; }
    public PaymentStatus getStatus() { return status; }
    public String getPaymentKey() { return paymentKey; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
}
