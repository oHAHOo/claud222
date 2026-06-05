package com.example.team3trimcommercepaymentproject.domain.order.entity;

import com.example.team3trimcommercepaymentproject.domain.member.entity.Member;
import com.example.team3trimcommercepaymentproject.domain.orderItem.entity.OrderItem;
import com.example.team3trimcommercepaymentproject.domain.payment.entity.Payment;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(
        name = "orders",
        indexes = {
                @Index(name = "idx_orders_member_created_at", columnList = "member_id, created_at"),
                @Index(name = "idx_orders_order_number", columnList = "order_number")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "order_number", nullable = false, unique = true, length = 100)
    private String orderNumber;

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
    private OrderStatus status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Payment payment;

    @Column(name = "cancel_reason")
    private String cancelReason;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "modified_at", nullable = false)
    private LocalDateTime modifiedAt;

    @Builder
    private Order(Member member, String orderNumber, Long totalAmount, Long usedPoint, Long pgAmount, Long earnedPoint) {
        this.member = member;
        this.orderNumber = orderNumber;
        this.totalAmount = totalAmount;
        this.usedPoint = usedPoint;
        this.pgAmount = pgAmount;
        this.earnedPoint = earnedPoint;
        this.status = OrderStatus.PAYMENT_PENDING;
    }

    public void addOrderItem(OrderItem orderItem) {
        this.orderItems.add(orderItem);
        orderItem.assignOrder(this);
    }

    public void assignPayment(Payment payment) {
        this.payment = payment;
        payment.assignOrder(this);
    }

    public void complete() {
        this.status = OrderStatus.COMPLETED;
    }

    public void cancel(String cancelReason) {
        this.status = OrderStatus.CANCELED;
        this.cancelReason = cancelReason;
        this.canceledAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.modifiedAt = now;
    }
}
