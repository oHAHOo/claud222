package com.example.team3trimcommercepaymentproject.domain.orderItem.entity;

import com.example.team3trimcommercepaymentproject.domain.order.entity.Order;
import com.example.team3trimcommercepaymentproject.domain.product.entity.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "order_items",
        indexes = {
                @Index(name = "idx_order_items_order_id", columnList = "order_id"),
                @Index(name = "idx_order_items_product_id", columnList = "product_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "product_name_snapshot", nullable = false, length = 255)
    private String productNameSnapshot;

    @Column(name = "price_snapshot", nullable = false)
    private Long priceSnapshot;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "refunded_quantity", nullable = false)
    private Integer refundedQuantity;

    @Column(name = "subtotal_amount", nullable = false)
    private Long subtotalAmount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "modified_at", nullable = false)
    private LocalDateTime modifiedAt;

    @Builder
    private OrderItem(Product product, String productNameSnapshot, Long priceSnapshot, Integer quantity) {
        this.product = product;
        this.productNameSnapshot = productNameSnapshot;
        this.priceSnapshot = priceSnapshot;
        this.quantity = quantity;
        this.refundedQuantity = 0;
        this.subtotalAmount = priceSnapshot * quantity;
    }

    public void assignOrder(Order order) {
        this.order = order;
    }

    public int getRefundableQuantity() {
        return quantity - refundedQuantity;
    }

    public void refundQuantity(int quantity) {
        if (quantity <= 0 || quantity > getRefundableQuantity()) {
            throw new IllegalArgumentException("환불 가능 수량을 초과했습니다.");
        }
        this.refundedQuantity += quantity;
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
