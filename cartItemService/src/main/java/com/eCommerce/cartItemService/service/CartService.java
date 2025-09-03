package com.eCommerce.cartItemService.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eCommerce.cartItemService.dto.ProductDTO;
import com.eCommerce.cartItemService.entity.CartItem;
import com.eCommerce.cartItemService.exception.ResourceNotFoundException;
import com.eCommerce.cartItemService.feign.ProductClient;
import com.eCommerce.cartItemService.repository.CartRepository;

import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    @Autowired
    private CartRepository repository;

    @Autowired
    private ProductClient productClient;

    public List<CartItem> getCartItems(Long userId) {
        return repository.findByUserId(userId);
    }

    public CartItem addToCart(CartItem item) {
        Optional<CartItem> existingItem = repository.findByUserIdAndProductId(item.getUserId(), item.getProductId());

        // Reserve product first — will throw if unavailable

        if (existingItem.isPresent()) {

            CartItem existing = existingItem.get();
            ProductDTO reservedProduct = productClient.reserveProduct(item.getProductId(), item.getQuantity());

            //existing.setQuantity(existing.getQuantity() + item.getQuantity());
            existing.setQuantity( item.getQuantity());
            existing.setTotalPrice(reservedProduct.getPrice().multiply(BigDecimal.valueOf(existing.getQuantity())));
            existing.setImageURL(reservedProduct.getImageURL());
            return repository.save(existing);
        } else {
            ProductDTO reservedProduct = productClient.reserveProduct(item.getProductId(), item.getQuantity());

            item.setTotalPrice(reservedProduct.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            item.setImageURL(reservedProduct.getImageURL());
            return repository.save(item);
        }
    }

    public CartItem updateCartItem(Long cartItemId, CartItem updated) {
        CartItem existing = repository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        // Reserve difference if increasing quantity
        int difference = updated.getQuantity() - existing.getQuantity();
        if (difference > 0) {
            // Increase in cart quantity → Reserve more stock
            productClient.reserveProduct(existing.getProductId(), updated.getQuantity());
        } else if (difference < 0) {
            // Decrease in cart quantity → Release stock back to product service
            productClient.releaseProduct(existing.getProductId(), Math.abs(difference));
        }
        ProductDTO product = productClient.getProductById(existing.getProductId());
        existing.setQuantity(updated.getQuantity());
        existing.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(updated.getQuantity())));
        return repository.save(existing);
    }

    public void removeCartItem(Long cartItemId) {
    	
    	
        if (!repository.existsById(cartItemId)) {
        	
            throw new ResourceNotFoundException("Cart item not found to delete.");
        }
        Optional<CartItem> cart = repository.findById(cartItemId);
        if (cart == null) {
        	throw new ResourceNotFoundException("Cart item not found to delete.");
        }

        // Return all quantity back to product stock
        //productClient.releaseProduct(cart.get().getProductId(), cart.get().getQuantity());
//        productClient.incrementProductStock(cart.get().getProductId(), cart.get().getQuantity());
        repository.deleteById(cartItemId);
    }

    public BigDecimal calculateTotalPrice(Long userId) {
        return repository.findByUserId(userId).stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    //when not ordered just simple removing
    @Transactional
    public void clearCartNotOrdered(Long userId) {
    	List<CartItem> cartItems = repository.findByUserId(userId);

        if (cartItems.isEmpty()) {
            return; // nothing to clear
        }

        // Release stock for each product before deleting
        for (CartItem item : cartItems) {
            productClient.releaseProduct(item.getProductId(), item.getQuantity());
        }
        repository.deleteByUserId(userId);
    }
    @Transactional
    public void clearCart(Long userId) {
    	
        repository.deleteByUserId(userId);
    }
}
