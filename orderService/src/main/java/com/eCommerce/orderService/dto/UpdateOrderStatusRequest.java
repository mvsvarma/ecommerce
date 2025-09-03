package com.eCommerce.orderService.dto;

public class UpdateOrderStatusRequest {
 private String status; // e.g., "SHIPPED", "DELIVERED", "CANCELLED"
 public String getStatus(){return status;}
 public void setStatus(String status){this.status=status;}
}
