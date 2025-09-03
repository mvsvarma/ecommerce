package com.eCommerce.cartItemService;

import com.eCommerce.cartItemService.controller.CartController;
import com.eCommerce.cartItemService.entity.CartItem;
import com.eCommerce.cartItemService.security.JwtUtils;
import com.eCommerce.cartItemService.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    @Mock private CartService service;
    @Mock private JwtUtils jwt;

    private MockMvc mvc;
    private final ObjectMapper om = new ObjectMapper();
    private static final String AUTH = "Bearer token-abc";
    private static final long UID = 42L;

    private static CartItem ci(Long id, long userId, long prodId, int qty, String total) {
        CartItem c = new CartItem();
        c.setCartItemId(id);
        c.setUserId(userId);
        c.setProductId(prodId);
        c.setQuantity(qty);
        if (total != null) c.setTotalPrice(new BigDecimal(total));
        return c;
    }

    @BeforeEach
    void setup() {
        CartController controller = new CartController();
        ReflectionTestUtils.setField(controller, "service", service);
        ReflectionTestUtils.setField(controller, "jwtUtils", jwt);
        mvc = MockMvcBuilders.standaloneSetup(controller).build();

        when(jwt.getUserIdFromJwtToken("token-abc")).thenReturn(UID);
    }

    @Test
    void getCart_ok() throws Exception {
        when(service.getCartItems(UID)).thenReturn(List.of(
                ci(1L, UID, 7L, 2, "25.00")
        ));

        mvc.perform(get("/api/cart").header("Authorization", AUTH))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$[0].cartItemId", is(1)))
           .andExpect(jsonPath("$[0].productId", is(7)));

        verify(service).getCartItems(UID);
    }

    @Test
    void total_ok() throws Exception {
        when(service.calculateTotalPrice(UID)).thenReturn(new BigDecimal("25.50"));

        mvc.perform(get("/api/cart/total").header("Authorization", AUTH))
           .andExpect(status().isOk())
           .andExpect(content().string("25.50"));

        verify(service).calculateTotalPrice(UID);
    }

    @Test
    void add_created() throws Exception {
        var body = """
          {"productId":7,"quantity":3}
        """;
        when(service.addToCart(org.mockito.ArgumentMatchers.any(CartItem.class)))
            .thenReturn(ci(10L, UID, 7L, 3, "30.00"));

        mvc.perform(post("/api/cart/add")
                .header("Authorization", AUTH)
                .contentType(APPLICATION_JSON)
                .content(body))
           .andExpect(status().isCreated())
           .andExpect(jsonPath("$.cartItemId", is(10)))
           .andExpect(jsonPath("$.quantity", is(3)))
           .andExpect(jsonPath("$.totalPrice", is(30.00)));

        verify(service).addToCart(org.mockito.ArgumentMatchers.any(CartItem.class));
    }

    @Test
    void update_ok() throws Exception {
        var body = """
          {"productId":7,"quantity":5}
        """;
        when(service.updateCartItem(eq(11L), org.mockito.ArgumentMatchers.any(CartItem.class)))
            .thenReturn(ci(11L, UID, 7L, 5, "50.00"));

        mvc.perform(put("/api/cart/update")
                .header("Authorization", AUTH)
                .param("cartItemId","11")
                .contentType(APPLICATION_JSON)
                .content(body))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.quantity", is(5)))
           .andExpect(jsonPath("$.totalPrice", is(50.00)));
    }

    @Test
    void clear_ok() throws Exception {
        mvc.perform(delete("/api/cart/clear").header("Authorization", AUTH))
           .andExpect(status().isNoContent());

        verify(service).clearCart(UID);
    }

    @Test
    void clearNotOrdered_ok() throws Exception {
        mvc.perform(delete("/api/cart/clearfromcart").header("Authorization", AUTH))
           .andExpect(status().isNoContent());

        verify(service).clearCartNotOrdered(UID);
    }
}
