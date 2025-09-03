package com.eCommerce.orderService.dto;
//com/eCommerce/orderService/dto/CartItemDTO.java

import java.math.BigDecimal;

public class CartItemDTO {
private Long cartItemId;
private Long userId;
private Long productId;
private Integer quantity;
private BigDecimal totalPrice;

public Long getCartItemId(){return cartItemId;} public void setCartItemId(Long v){cartItemId=v;}
public Long getUserId(){return userId;} public void setUserId(Long v){userId=v;}
public Long getProductId(){return productId;} public void setProductId(Long v){productId=v;}
public Integer getQuantity(){return quantity;} public void setQuantity(Integer v){quantity=v;}
public BigDecimal getTotalPrice(){return totalPrice;} public void setTotalPrice(BigDecimal v){totalPrice=v;}
}
