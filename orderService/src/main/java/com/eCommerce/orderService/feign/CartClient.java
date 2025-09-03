package com.eCommerce.orderService.feign;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import com.eCommerce.orderService.dto.CartItemDTO;

import java.util.List;

@FeignClient(name = "cartItemService")
public interface CartClient {
	  @GetMapping("/api/cart")
	  List<CartItemDTO> getCart();

	  // Prefer this (no stock release on checkout clear)
	  @DeleteMapping("/api/cart/clear")
	  void clearCart();

	  // Fallback (NOT recommended on checkout because it releases stock):
	  @DeleteMapping("/api/cart/remove")
	  void removeCartItem(@RequestParam("cartItemId") Long cartItemId);
	}
