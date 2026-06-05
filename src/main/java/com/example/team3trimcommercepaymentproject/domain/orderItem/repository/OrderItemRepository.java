package com.example.team3trimcommercepaymentproject.domain.orderItem.repository;

import com.example.team3trimcommercepaymentproject.domain.orderItem.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long>{
}
