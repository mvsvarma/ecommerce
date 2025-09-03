package com.eCommerce.orderService.dto;


public class DirectBuyRequest {
private Long addressId;
private Long productId;
private Integer quantity; // optional, defaults to 1

public Long getAddressId() { return addressId; }
public void setAddressId(Long addressId) { this.addressId = addressId; }
public Long getProductId() { return productId; }
public void setProductId(Long productId) { this.productId = productId; }
public Integer getQuantity() { return quantity; }
public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
