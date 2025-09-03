package com.eCommerce.orderService.repository;

//src/main/java/com/eCommerce/orderService/repository/OrderRepository.java

import com.eCommerce.orderService.entity.Order;
import com.eCommerce.orderService.entity.OrderStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
 List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);
 Optional<Order> findByIdAndUserId(Long id, Long userId);

 // admin filters
 List<Order> findByOrderStatusOrderByCreatedAtDesc(OrderStatus status);
 List<Order> findByUserIdAndOrderStatusOrderByCreatedAtDesc(Long userId, OrderStatus status);
 List<Order> findByUserId(Long userId);
}

