package com.eCommerce.orderService.service;
//com/eCom// src/main/java/com/eCommerce/orderService/service/CheckoutService.java

import com.eCommerce.orderService.dto.*;
import com.eCommerce.orderService.entity.*;
import com.eCommerce.orderService.feign.AddressClient;
import com.eCommerce.orderService.feign.CartClient;
import com.eCommerce.orderService.feign.ProductClient;
import com.eCommerce.orderService.mappers.AddressMapper;
import com.eCommerce.orderService.mappers.OrderMapper;
import com.eCommerce.orderService.repository.OrderRepository;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CheckoutService {

    private final OrderRepository orders;
    private final CartClient cartClient;
    private final AddressClient addressClient;
    private final ProductClient productClient;
    @Autowired
    AddressMapper addressMapper;
    @Autowired
    OrderMapper orderMapper;

    public CheckoutService(OrderRepository orders, CartClient cartClient,
                           AddressClient addressClient, ProductClient productClient) {
        this.orders = orders;
        this.cartClient = cartClient;
        this.addressClient = addressClient;
        this.productClient = productClient;
    }

    /** 1) Address selection list for the logged-in user */
    public List<AddressResponseDTO> listMyAddresses() {
        return addressClient.listMyAddresses(); // JWT forwarded by your interceptor
    }

    /** 2) Checkout: snapshot address, pull cart, create order+items, clear cart (no stock release), dummy paid */
    @Transactional
    public OrderResponse checkout(Long userId, Long addressId) {
        // A) snapshot address
        EmbeddedAddress addr = addressMapper.toEmbedded(addressClient.getShipping(addressId));
        if (addr == null) throw new RuntimeException("Address not found or not owned");

        // B) read cart
        List<CartItemDTO> cart = cartClient.getCart();
        if (cart == null || cart.isEmpty()) throw new RuntimeException("Your cart is empty");

        // C) build order skeleton
        Order order = new Order();
        order.setUserId(userId);
        order.setAddress(addr);
        order.setOrderStatus(OrderStatus.PENDING);
        order.setPaymentStatus(PaymentStatus.PAID); // dummy

        // D) map cart -> order items (reserve + snapshot product)
        List<OrderItem> items = cart.stream().map(ci -> {
            // stock reservation BEFORE persisting
            productClient.reserveProduct(ci.getProductId(), ci.getQuantity());
            ProductDTO p = productClient.getById(ci.getProductId());
            if (p == null) throw new RuntimeException("Product not found: " + ci.getProductId());

            OrderItem it = new OrderItem();
            it.setOrder(order);
            it.setProductId(ci.getProductId());
            it.setProductName(p.getName());
            it.setPrice(p.getPrice());
            it.setQuantity(ci.getQuantity());
            return it;
        }).toList();

        order.setItems(items);

        // E) compute total
        BigDecimal total = items.stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(total);

        // F) persist + clear cart
        Order saved = orders.save(order);
        try { cartClient.clearCart(); } catch (Exception ignored) {}

        // G) response
        return orderMapper.toResponse(saved);
    }
    
    
    
    ///BUY NOW OPTION TO CHECKOUT DIRECTLY
    @Transactional
    public OrderResponse buyNow(Long userId, DirectBuyRequest req) {
      int qty = (req.getQuantity() == null || req.getQuantity() < 1) ? 1 : req.getQuantity();

      // snapshot address
      EmbeddedAddress addr = addressMapper.toEmbedded(addressClient.getShipping(req.getAddressId()));
      if (addr == null) throw new RuntimeException("Address not found / not owned");

      boolean reserved = false;
      try {
        // reserve before persisting
        productClient.reserveProduct(req.getProductId(), qty);
        reserved = true;

        // product snapshot
        ProductDTO p = productClient.getById(req.getProductId());
        if (p == null) throw new RuntimeException("Product not found: " + req.getProductId());

        // build order
        Order order = new Order();
        order.setUserId(userId);
        order.setAddress(addr);
        order.setOrderStatus(OrderStatus.PENDING);
        order.setPaymentStatus(PaymentStatus.PAID); // dummy
        order.setTotalAmount(p.getPrice().multiply(BigDecimal.valueOf(qty)));

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProductId(req.getProductId());
        item.setProductName(p.getName());
        item.setPrice(p.getPrice());
        item.setQuantity(qty);
        order.setItems(List.of(item));

        Order saved = orders.save(order);
        return orderMapper.toResponse(saved);

      } catch (RuntimeException ex) {
        if (reserved) {
          try { productClient.releaseProduct(req.getProductId(), qty); } catch (Exception ignored) {}
        }
        throw ex;
      }
    }
}
