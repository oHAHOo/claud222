package com.example.team3trimcommercepaymentproject.domain.payment.entity;

import com.example.team3trimcommercepaymentproject.domain.order.entity.Order;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "payments",
        indexes = {
                @Index(name = "idx_payments_order_id", columnList = "order_id"),
                @Index(name = "idx_payments_portone_payment_id", columnList = "portone_payment_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Column(name = "portone_payment_id", nullable = false, unique = true, length = 255)
    private String portonePaymentId;

    @Column(name = "total_amount", nullable = false)
    private Long totalAmount;

    @Column(name = "used_point", nullable = false)
    private Long usedPoint;

    @Column(name = "pg_amount", nullable = false)
    private Long pgAmount;

    @Column(name = "earned_point", nullable = false)
    private Long earnedPoint;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private PaymentStatus status;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "modified_at", nullable = false)
    private LocalDateTime modifiedAt;

    @Builder
    private Payment(String portonePaymentId, Long totalAmount, Long usedPoint, Long pgAmount, Long earnedPoint) {
        this.portonePaymentId = portonePaymentId;
        this.totalAmount = totalAmount;
        this.usedPoint = usedPoint;
        this.pgAmount = pgAmount;
        this.earnedPoint = earnedPoint;
        this.status = PaymentStatus.READY;
    }

    public void assignOrder(Order order) {
        this.order = order;
    }

    public boolean isPaid() {
        return this.status == PaymentStatus.PAID;
    }

    public void complete() {
        if (this.status == PaymentStatus.PAID) {
            return;
        }
        this.status = PaymentStatus.PAID;
        this.paidAt = LocalDateTime.now();
    }

    public void fail() {
        this.status = PaymentStatus.FAILED;
    }

    public void cancel() {
        this.status = PaymentStatus.CANCELED;
    }

    public void refund() {
        this.status = PaymentStatus.REFUNDED;
    }

    public void partialRefund() {
        this.status = PaymentStatus.PARTIAL_REFUNDED;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.modifiedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.modifiedAt = LocalDateTime.now();
    }
}