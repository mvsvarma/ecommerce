package com.eCommerce.cartItemService;

import com.eCommerce.cartItemService.dto.ProductDTO;
import com.eCommerce.cartItemService.entity.CartItem;
import com.eCommerce.cartItemService.exception.ResourceNotFoundException;
import com.eCommerce.cartItemService.feign.ProductClient;
import com.eCommerce.cartItemService.repository.CartRepository;
import com.eCommerce.cartItemService.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock private CartRepository repo;
    @Mock private ProductClient productClient;

    @InjectMocks private CartService svc;

    private static CartItem item(long userId, long productId, int qty) {
        CartItem c = new CartItem();
        c.setUserId(userId);
        c.setProductId(productId);
        c.setQuantity(qty);
        return c;
    }

    private static ProductDTO product(long id, String price) {
        ProductDTO p = new ProductDTO();
        p.setProductID(id);
        p.setPrice(new BigDecimal(price));
        return p;
    }

    @BeforeEach
    void resetMocks() {
        clearInvocations(repo, productClient);
    }

    @Test
    void addToCart_creates_whenNew() {
        var in = item(99L, 7L, 3);
        when(repo.findByUserIdAndProductId(99L, 7L)).thenReturn(Optional.empty());
        when(productClient.reserveProduct(7L, 3)).thenReturn(product(7L, "12.50"));
        when(repo.save(any(CartItem.class))).thenAnswer(a -> {
            CartItem saved = a.getArgument(0);
            saved.setCartItemId(111L);
            return saved;
        });

        CartItem out = svc.addToCart(in);

        assertThat(out.getCartItemId()).isEqualTo(111L);
        assertThat(out.getTotalPrice()).isEqualByComparingTo("37.50");
        verify(productClient).reserveProduct(7L, 3);
        verify(repo).save(any(CartItem.class));
    }

    @Test
    void addToCart_updates_whenExisting() {
        var existing = item(99L, 7L, 2);
        existing.setCartItemId(55L);
        var incoming = item(99L, 7L, 5); // controller wants to set qty to 5

        when(repo.findByUserIdAndProductId(99L, 7L)).thenReturn(Optional.of(existing));
        when(productClient.reserveProduct(7L, 5)).thenReturn(product(7L, "10.00"));
        when(repo.save(any(CartItem.class))).thenAnswer(a -> a.getArgument(0));

        CartItem out = svc.addToCart(incoming);

        assertThat(out.getCartItemId()).isEqualTo(55L);
        assertThat(out.getQuantity()).isEqualTo(5);
        assertThat(out.getTotalPrice()).isEqualByComparingTo("50.00");
        verify(productClient).reserveProduct(7L, 5);
    }

    @Test
    void updateCartItem_increase_reservesDifference() {
        var existing = item(99L, 7L, 2);
        existing.setCartItemId(70L);
        when(repo.findById(70L)).thenReturn(Optional.of(existing));
        when(productClient.reserveProduct(7L, 3)).thenReturn(product(7L, "9.99")); // only used for side-effect
        when(productClient.getProductById(7L)).thenReturn(product(7L, "9.99"));
        when(repo.save(any(CartItem.class))).thenAnswer(a -> a.getArgument(0));

        var updated = item(99L, 7L, 5);

        CartItem out = svc.updateCartItem(70L, updated);

        assertThat(out.getQuantity()).isEqualTo(5);
        assertThat(out.getTotalPrice()).isEqualByComparingTo("49.95");
        verify(productClient).reserveProduct(7L, 3);
        verify(productClient).getProductById(7L);
    }

    @Test
    void updateCartItem_decrease_releasesDifference() {
        var existing = item(99L, 7L, 6);
        existing.setCartItemId(71L);
        when(repo.findById(71L)).thenReturn(Optional.of(existing));
        when(productClient.getProductById(7L)).thenReturn(product(7L, "20.00"));
        when(repo.save(any(CartItem.class))).thenAnswer(a -> a.getArgument(0));

        var updated = item(99L, 7L, 2);

        CartItem out = svc.updateCartItem(71L, updated);

        assertThat(out.getQuantity()).isEqualTo(2);
        assertThat(out.getTotalPrice()).isEqualByComparingTo("40.00");
        verify(productClient).releaseProduct(7L, 4);
    }

    @Test
    void removeCartItem_throws_whenMissing() {
        when(repo.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> svc.removeCartItem(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");

        verify(repo, never()).deleteById(anyLong());
    }

    @Test
    void removeCartItem_deletes_whenPresent() {
        var ex = item(1L, 2L, 3);
        ex.setCartItemId(5L);
        when(repo.existsById(5L)).thenReturn(true);
        when(repo.findById(5L)).thenReturn(Optional.of(ex));

        svc.removeCartItem(5L);

        verify(repo).deleteById(5L);
        // Note: current code does not call productClient.releaseProduct here
    }

    @Test
    void calculateTotalPrice_sums() {
        var a = item(9L, 1L, 1); a.setTotalPrice(new BigDecimal("10.00"));
        var b = item(9L, 2L, 2); b.setTotalPrice(new BigDecimal("5.50"));
        when(repo.findByUserId(9L)).thenReturn(List.of(a, b));

        assertThat(svc.calculateTotalPrice(9L)).isEqualByComparingTo("15.50");
    }

    @Test
    void clearCartNotOrdered_releasesAndDeletes() {
        var a = item(9L, 1L, 2);
        var b = item(9L, 2L, 3);
        when(repo.findByUserId(9L)).thenReturn(List.of(a, b));

        svc.clearCartNotOrdered(9L);

        verify(productClient).releaseProduct(1L, 2);
        verify(productClient).releaseProduct(2L, 3);
        verify(repo).deleteByUserId(9L);
    }

    @Test
    void clearCart_deletesOnly() {
        svc.clearCart(77L);
        verify(repo).deleteByUserId(77L);
        verifyNoInteractions(productClient);
    }
}
