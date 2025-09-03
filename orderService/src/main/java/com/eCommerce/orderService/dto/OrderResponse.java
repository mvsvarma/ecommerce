package com.eCommerce.orderService.dto;
//com/eCommerce/orderService/dto/OrderResponse.java

import com.eCommerce.orderService.entity.EmbeddedAddress;
import com.eCommerce.orderService.entity.OrderStatus;
import com.eCommerce.orderService.entity.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderResponse {
private Long orderId;
private Long userId;
private EmbeddedAddress address;
private BigDecimal totalAmount;
private OrderStatus orderStatus;
private PaymentStatus paymentStatus;
private LocalDateTime createdAt;
private LocalDateTime updatedAt;
private List<OrderItemResponse> items;

// getters/setters...
public Long getOrderId(){return orderId;} public void setOrderId(Long v){orderId=v;}
public Long getUserId(){return userId;} public void setUserId(Long v){userId=v;}
public EmbeddedAddress getAddress(){return address;} public void setAddress(EmbeddedAddress v){address=v;}
public BigDecimal getTotalAmount(){return totalAmount;} public void setTotalAmount(BigDecimal v){totalAmount=v;}
public OrderStatus getOrderStatus(){return orderStatus;} public void setOrderStatus(OrderStatus v){orderStatus=v;}
public PaymentStatus getPaymentStatus(){return paymentStatus;} public void setPaymentStatus(PaymentStatus v){paymentStatus=v;}
public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
public LocalDateTime getUpdatedAt(){return updatedAt;} public void setUpdatedAt(LocalDateTime v){updatedAt=v;}
public List<OrderItemResponse> getItems(){return items;} public void setItems(List<OrderItemResponse> v){items=v;}
}
