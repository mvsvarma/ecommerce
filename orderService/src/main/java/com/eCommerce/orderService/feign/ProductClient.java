package com.eCommerce.orderService.feign;



import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import com.eCommerce.orderService.dto.ProductDTO;

import java.util.Map;

@FeignClient(name = "productService")
public interface ProductClient {
	  @GetMapping("/api/products/{id}")
	  ProductDTO getById(@PathVariable("id") Long id);
	  
	  @PostMapping("/api/products/{id}/reserve")
	  ProductDTO reserveProduct(
	          @PathVariable("id") Long id,
	          @RequestParam("quantity") int quantity
	  );
	  @PostMapping("/api/products/{id}/release")
	  ProductDTO releaseProduct(@PathVariable("id") Long id, @RequestParam("quantity") int quantity);

	}