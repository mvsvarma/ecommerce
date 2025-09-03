package com.eCommerce.orderService.service;

import com.eCommerce.orderService.dto.OrderResponse;
import com.eCommerce.orderService.entity.*;
import com.eCommerce.orderService.feign.ProductClient;
import com.eCommerce.orderService.mappers.OrderMapper;
import com.eCommerce.orderService.repository.OrderRepository;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderManagementService {

 private final OrderRepository orders;
 private final ProductClient productClient;
 @Autowired
 OrderMapper orderMapper;


 public OrderManagementService(OrderRepository orders, ProductClient productClient) {
     this.orders = orders;
     this.productClient = productClient;
     
 }
 // -------------------- USER: My orders / track / cancel --------------------

 public List<OrderResponse> myOrders(Long userId) {
     return orders.findByUserIdOrderByCreatedAtDesc(userId)
             .stream().map(orderMapper::toResponse).toList();
 }

 public OrderResponse getMine(Long userId, Long orderId) {
     Order o = orders.findByIdAndUserId(orderId, userId)
             .orElseThrow(() -> new RuntimeException("Order not found"));
     return orderMapper.toResponse(o);
 }

 public String track(Long userId, Long orderId) {
     Order o = orders.findByIdAndUserId(orderId, userId)
             .orElseThrow(() -> new RuntimeException("Order not found"));
     return o.getOrderStatus().name();
 }

 @Transactional
 public OrderResponse cancel(Long userId, Long orderId) {
     Order o = orders.findByIdAndUserId(orderId, userId)
             .orElseThrow(() -> new RuntimeException("Order not found"));

     if (o.getOrderStatus() != OrderStatus.PENDING) {
         throw new RuntimeException("Order cannot be cancelled at this stage");
     }

     // release inventory for each line
     for (OrderItem it : o.getItems()) {
         try { productClient.releaseProduct(it.getProductId(), it.getQuantity()); 
         }
         catch (Exception ignored) {}
     }

     o.setOrderStatus(OrderStatus.CANCELLED);
     o.setPaymentStatus(PaymentStatus.REFUND_UNDER_PROCESS); // dummy refund IPPUDHE MARCHA
     return orderMapper.toResponse(orders.save(o));
 }

 // -------------------- ADMIN: list / get / update-status / refund --------------------

 public List<OrderResponse> adminList(String statusOpt, Long userIdOpt) {
     if (statusOpt != null && userIdOpt != null) {
         OrderStatus st = OrderStatus.valueOf(statusOpt.toUpperCase());
         return orders.findByUserIdAndOrderStatusOrderByCreatedAtDesc(userIdOpt, st)
                 .stream().map(orderMapper::toResponse).toList();
     }
     if (statusOpt != null) {
         OrderStatus st = OrderStatus.valueOf(statusOpt.toUpperCase());
         return orders.findByOrderStatusOrderByCreatedAtDesc(st)
                 .stream().map(orderMapper::toResponse).toList();
     }
     if (userIdOpt != null) {
         return orders.findByUserId(userIdOpt).stream().map(orderMapper::toResponse).toList();
     }
     return orders.findAll().stream().map(orderMapper::toResponse).toList();
 }

 public OrderResponse adminGet(Long orderId) {
     Order o = orders.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
     return orderMapper.toResponse(o);
 }

 @Transactional
 public OrderResponse adminUpdateStatus(Long orderId, String newStatus) {
     Order o = orders.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
     OrderStatus st = OrderStatus.valueOf(newStatus.toUpperCase());
     o.setOrderStatus(st);
     return orderMapper.toResponse(orders.save(o));
 }

 @Transactional
 public OrderResponse adminRefund(Long orderId, String reason) {
     Order o = orders.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
     o.setPaymentStatus(PaymentStatus.REFUNDED);
     return orderMapper.toResponse(orders.save(o));
 }
}


