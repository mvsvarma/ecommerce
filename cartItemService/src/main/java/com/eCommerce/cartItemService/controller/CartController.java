package com.eCommerce.cartItemService.controller;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.eCommerce.cartItemService.entity.CartItem;
import com.eCommerce.cartItemService.security.JwtUtils;
import com.eCommerce.cartItemService.service.CartService;

import java.math.BigDecimal;
import java.util.List;

@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService service;

    @Autowired
    private JwtUtils jwtUtils;

    private Long getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return jwtUtils.getUserIdFromJwtToken(token);
    }

    @GetMapping
    public List<CartItem> getCart(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        return service.getCartItems(userId);
    }

    @GetMapping("/total")
    public BigDecimal getTotalCartPrice(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        return service.calculateTotalPrice(userId);
    }

    @PostMapping("/add")
    @ResponseStatus(HttpStatus.CREATED)
    public CartItem addItem(HttpServletRequest request, @RequestBody CartItem item) {
        Long userId = getUserIdFromRequest(request);
        item.setUserId(userId);
        return service.addToCart(item);
    }

    @PutMapping("/update")
    public CartItem updateItem(HttpServletRequest request,
                               @RequestParam Long cartItemId,
                               @RequestBody CartItem item) {
        Long userId = getUserIdFromRequest(request);
        item.setUserId(userId);
        return service.updateCartItem(cartItemId, item);
    }

    @DeleteMapping("/remove")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteItem(@RequestParam Long cartItemId) {
        service.removeCartItem(cartItemId);
    }
    @DeleteMapping("/clear")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearCart(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        service.clearCart(userId);
    }
    
    @DeleteMapping("/clearfromcart")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearCartNotOrdered(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        service.clearCartNotOrdered(userId);
    }
}
