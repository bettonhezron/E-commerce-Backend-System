package com.hezron.ecommerce.service;

import com.hezron.ecommerce.dto.OrderDTO;
import com.hezron.ecommerce.dto.OrderRequestDTO;

import java.util.List;

public interface OrderService {

    OrderDTO placeOrder(OrderRequestDTO orderRequest);

    OrderDTO getOrderById(Long id);

    List<OrderDTO> getCurrentUserOrders();
}
