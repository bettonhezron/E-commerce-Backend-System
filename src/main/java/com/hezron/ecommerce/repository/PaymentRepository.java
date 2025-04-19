package com.hezron.ecommerce.repository;



import com.hezron.ecommerce.model.Order;
import com.hezron.ecommerce.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByTransactionId(String transactionId);
    Optional<Payment> findByOrder(Order order);
}
