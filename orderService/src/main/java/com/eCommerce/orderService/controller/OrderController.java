package com.eCommerce.orderService.controller;

//src/main/java/com/eCommerce/orderService/controller/CheckoutController.java

import com.eCommerce.orderService.dto.*;
import com.eCommerce.orderService.entity.OrderStatus;
import com.eCommerce.orderService.security.JwtUtils;
import com.eCommerce.orderService.service.CheckoutService;
import com.eCommerce.orderService.service.OrderManagementService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/checkout")
public class OrderController {
	@Autowired
	private JwtUtils jwtUtils;
	@Autowired
	OrderManagementService managementService;
	

 private final CheckoutService checkout;

 // If you have a JwtUtils bean already, inject and parse userId.
 // Here we expect the gateway/security layer to set userId header for simplicity.
 public OrderController(CheckoutService checkout) {
     this.checkout = checkout;
 }

 private Long getUserIdFromRequest(HttpServletRequest request) {
     String token = request.getHeader("Authorization").substring(7);
     return jwtUtils.getUserIdFromJwtToken(token);
 }
 
 private Long uid(String auth) {
     String token = auth.replace("Bearer ", "");
     return jwtUtils.getUserIdFromJwtToken(token);
 }

 // Address selection for the logged-in user
 @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
 @GetMapping("/addresses")
 public List<AddressResponseDTO> myAddresses() {
     return checkout.listMyAddresses();
 }

 // Checkout with selected address
 @PostMapping
 @ResponseStatus(HttpStatus.CREATED)
 public OrderResponse checkout(HttpServletRequest request,  // or extract via JwtUtils if you prefer
                               @RequestBody CreateOrderRequest req) {
	 Long userId = getUserIdFromRequest(request);
     return checkout.checkout(userId, req.getAddressId());
 }
 

 @PostMapping("/buy-now")
 @ResponseStatus(HttpStatus.CREATED)
 public OrderResponse buyNow(HttpServletRequest request,
                             @RequestBody DirectBuyRequest req) {
	 Long userId = getUserIdFromRequest(request);
	 return checkout.buyNow(userId, req);
 }
 

 // My orders
 @GetMapping("/me/orders")
 @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
 public List<OrderResponse> myOrders(@RequestHeader("Authorization") String auth) {
     return managementService.myOrders(uid(auth));
 }

 // Get my order
 @GetMapping("/me/orders/{orderId}")
 @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
 public OrderResponse getMine(@RequestHeader("Authorization") String auth,
                              @PathVariable Long orderId) {
     return managementService.getMine(uid(auth), orderId);
 }

 // Track my order
 @GetMapping("/me/orders/{orderId}/track")
 @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
 public String track(@RequestHeader("Authorization") String auth,
                     @PathVariable Long orderId) {
     return managementService.track(uid(auth), orderId);
 }

 // Cancel my order
 @PutMapping("/me/orders/{orderId}/cancel")
 @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
 public OrderResponse cancel(@RequestHeader("Authorization") String auth,
                             @PathVariable Long orderId) {
     return managementService.cancel(uid(auth), orderId);
 }

 // -------------------- ADMIN endpoints --------------------

 // List all orders with optional filters
 @GetMapping("/admin/orders")
 @PreAuthorize("hasRole('ROLE_ADMIN')")
 public List<OrderResponse> adminList(@RequestParam(required = false) String status,
                                      @RequestParam(required = false) Long userId) {
     return managementService.adminList(status, userId);
 }

 // Get order by id
 @GetMapping("/admin/orders/{orderId}")
 @PreAuthorize("hasRole('ROLE_ADMIN')")
 public OrderResponse adminGet(@PathVariable Long orderId) {
     return managementService.adminGet(orderId);
 }

 // Update order status (PENDING/SHIPPED/DELIVERED/CANCELLED)
 @PutMapping("/admin/orders/{orderId}/status")
 @PreAuthorize("hasRole('ROLE_ADMIN')")
 public OrderResponse adminUpdateStatus(@PathVariable Long orderId,
                                        @RequestBody UpdateOrderStatusRequest req) {
     // optional guard to validate enum early
     OrderStatus.valueOf(req.getStatus().toUpperCase());
     return managementService.adminUpdateStatus(orderId, req.getStatus());
 }

 // Mark payment REFUNDED (dummy)
 @PutMapping("/admin/orders/{orderId}/refund")
 @ResponseStatus(HttpStatus.OK)
 @PreAuthorize("hasRole('ROLE_ADMIN')")
 public OrderResponse adminRefund(@PathVariable Long orderId,
                                  @RequestBody(required = false) RefundRequest req) {
     String reason = (req == null ? null : req.getReason());
     return managementService.adminRefund(orderId, reason);
 }
}
