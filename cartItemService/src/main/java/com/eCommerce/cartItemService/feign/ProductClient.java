package com.eCommerce.cartItemService.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import com.eCommerce.cartItemService.dto.ProductDTO;

@FeignClient(name = "productService")
public interface ProductClient {

    @GetMapping("/api/products/{id}")
    ProductDTO getProductById(@PathVariable("id") Long id);
    
    @PostMapping("/api/products/{id}/CheckAvailability")
    ProductDTO reserveProduct(
            @PathVariable("id") Long id,
            @RequestParam("quantity") int quantity
    );
    
    @PostMapping("/api/products/{id}/release")
    ProductDTO releaseProduct(@PathVariable("id") Long id,
                              @RequestParam("quantity") int quantity);
}
