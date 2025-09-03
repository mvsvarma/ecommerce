package com.eCommerce.orderService.repository;
// src/main/java/com/eCommerce/orderService/repository/OrderItemRepository.java

import com.eCommerce.orderService.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {}
