package com.hezron.ecommerce.service;

import com.hezron.ecommerce.dto.OrderDTO;
import com.hezron.ecommerce.dto.OrderRequestDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface OrderService {

    OrderDTO placeOrder(OrderRequestDTO orderRequest);

    @Transactional
    OrderDTO placeOrder(OrderRequestDTO orderRequest, String username);

    OrderDTO getOrderById(Long id);

    OrderDTO getOrderById(Long id, String username);

    List<OrderDTO> getCurrentUserOrders();

    List<OrderDTO> getCurrentUserOrders(String username);

    @Transactional
    OrderDTO updateOrderStatus(Long orderId, String status);
}
