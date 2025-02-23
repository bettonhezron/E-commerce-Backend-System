package com.hezron.ecommerce.repository;

import com.hezron.ecommerce.model.Order;
import com.hezron.ecommerce.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);
    List<Order> findByStatus(OrderStatus status);
}
