package com.eCommerce.orderService.mappers;

//src/main/java/com/eCommerce/orderService/mapper/OrderMapper.java

import com.eCommerce.orderService.dto.OrderItemResponse;
import com.eCommerce.orderService.dto.OrderResponse;
import com.eCommerce.orderService.entity.Order;
import com.eCommerce.orderService.entity.OrderItem;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderMapper {

 public OrderResponse toResponse(Order o) {
     OrderResponse r = new OrderResponse();
     r.setOrderId(o.getId());
     r.setUserId(o.getUserId());
     r.setAddress(o.getAddress());
     r.setTotalAmount(o.getTotalAmount());
     r.setOrderStatus(o.getOrderStatus());
     r.setPaymentStatus(o.getPaymentStatus());
     r.setCreatedAt(o.getCreatedAt());
     r.setUpdatedAt(o.getUpdatedAt());
     r.setItems(toItemResponses(o.getItems()));
     return r;
 }

 public List<OrderItemResponse> toItemResponses(List<OrderItem> items) {
     return items.stream().map(this::toItemResponse).toList();
 }

 public OrderItemResponse toItemResponse(OrderItem i) {
     OrderItemResponse r = new OrderItemResponse();
     r.setProductId(i.getProductId());
     r.setProductName(i.getProductName());
     r.setPrice(i.getPrice());
     r.setQuantity(i.getQuantity());
     return r;
 }
}
