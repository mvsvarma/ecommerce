package com.eCommerce.orderService.dto;
//com/eCommerce/orderService/dto/OrderItemResponse.java

import java.math.BigDecimal;

public class OrderItemResponse {
private Long productId;
private String productName;
private BigDecimal price;
private Integer quantity;

public Long getProductId(){return productId;} public void setProductId(Long v){productId=v;}
public String getProductName(){return productName;} public void setProductName(String v){productName=v;}
public BigDecimal getPrice(){return price;} public void setPrice(BigDecimal v){price=v;}
public Integer getQuantity(){return quantity;} public void setQuantity(Integer v){quantity=v;}
}
