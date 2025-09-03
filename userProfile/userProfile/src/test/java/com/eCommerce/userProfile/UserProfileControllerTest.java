package com.eCommerce.userProfile;

import com.eCommerce.userProfile.controller.UserProfileController;
import com.eCommerce.userProfile.dto.AddressRequest;
import com.eCommerce.userProfile.dto.AddressResponse;
import com.eCommerce.userProfile.dto.ShippingAddressDTO;
import com.eCommerce.userProfile.dto.UserProfileRequest;
import com.eCommerce.userProfile.dto.UserProfileResponse;
import com.eCommerce.userProfile.entity.UserProfile;
import com.eCommerce.userProfile.feign.AddressClient;
import com.eCommerce.userProfile.repository.UserProfileRepository;
import com.eCommerce.userProfile.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller slice tests – method security honored via @WithMockUser,
 * security filter chain disabled for simplicity.
 */
@WebMvcTest(controllers = UserProfileController.class)
@AutoConfigureMockMvc(addFilters = false)
@EnableMethodSecurity
class UserProfileControllerTest {

    @Autowired private MockMvc mvc;

    @MockBean private UserProfileRepository repo;
    @MockBean private JwtUtils jwtUtils;
    @MockBean private AddressClient addressClient;

    private static final String AUTH = "Bearer token123";
    private static final long USER_ID = 42L;

    private UserProfile entity;

    private AddressResponse addr(long id) {
        AddressResponse d = new AddressResponse();
        d.setId(id);
        d.setUserId(USER_ID);
        d.setFullName("Alice");
        d.setPhone("111");
        d.setLine1("Main");
        d.setCity("Pune");
        d.setState("MH");
        d.setDefaultShipping(false);
        d.setCreatedAt(LocalDateTime.now());
        d.setUpdatedAt(LocalDateTime.now());
        return d;
    }

    @BeforeEach
    void setUp() {
        entity = new UserProfile();
        entity.setId(USER_ID);
        entity.setFullName("John Doe");
        entity.setAddress("Addr 1");
        entity.setPhoneNumber("999");
    }

    // ---------- /api/profile (self) ----------

    @Test
    @WithMockUser(roles = "USER")
    void getProfile_me_ok() throws Exception {
        when(jwtUtils.getUserIdFromJwtToken("token123")).thenReturn(USER_ID);
        when(repo.findById(USER_ID)).thenReturn(Optional.of(entity));

        mvc.perform(get("/api/profile/me").header("Authorization", AUTH))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.id", is(42)))
           .andExpect(jsonPath("$.fullName", is("John Doe")));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createOrUpdateProfile_me_ok() throws Exception {
        when(jwtUtils.getUserIdFromJwtToken("token123")).thenReturn(USER_ID);

        String body = """
          {"fullName":"Jane","address":"New Addr","phoneNumber":"123"}
        """;

        mvc.perform(post("/api/profile/me")
                .header("Authorization", AUTH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
           .andExpect(status().isOk())
           .andExpect(content().string(containsString("Profile updated")));

        verify(repo).save(any(UserProfile.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void admin_getById_and_delete_ok() throws Exception {
        when(repo.findById(7L)).thenReturn(Optional.of(entity));
        mvc.perform(get("/api/profile/7"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.fullName", is("John Doe")));

        when(repo.existsById(7L)).thenReturn(true);
        mvc.perform(delete("/api/profile/7"))
           .andExpect(status().isOk())
           .andExpect(content().string(containsString("Profile deleted")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void admin_getAll_ok() throws Exception {
        when(repo.findAll()).thenReturn(List.of(entity));

        mvc.perform(get("/api/profile/all"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$[0].id", is(42)));
    }

    // ---------- Proxied Address endpoints ----------

    @Test
    @WithMockUser(roles = "USER")
    void listMyAddresses_ok() throws Exception {
        when(addressClient.listMyAddresses()).thenReturn(List.of(addr(1), addr(2)));

        mvc.perform(get("/api/profile/me/addresses").header("Authorization", AUTH))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$", hasSize(2)))
           .andExpect(jsonPath("$[0].id", is(1)));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getMyAddress_ok() throws Exception {
        when(addressClient.getMyAddress(5L)).thenReturn(addr(5));

        mvc.perform(get("/api/profile/me/addresses/5").header("Authorization", AUTH))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.id", is(5)));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getMyShippingSnapshot_ok() throws Exception {
        ShippingAddressDTO snap = new ShippingAddressDTO();
        snap.setFullName("Alice"); snap.setLine1("Main");
        snap.setCity("Pune"); snap.setState("MH"); snap.setPhone("111");

        when(addressClient.getShipping(8L)).thenReturn(snap);

        mvc.perform(get("/api/profile/me/addresses/8/shipping").header("Authorization", AUTH))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.line1", is("Main")));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createAddress_ok() throws Exception {
        AddressResponse out = addr(10);
        when(addressClient.create(any(AddressRequest.class))).thenReturn(out);

        String body = """
          {"fullName":"Alice","phone":"111","line1":"Main","city":"Pune","state":"MH","defaultShipping":true}
        """;

        mvc.perform(post("/api/profile/me/addresses")
                .header("Authorization", AUTH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.id", is(10)));
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateAddress_ok() throws Exception {
        AddressResponse out = addr(3);
        out.setFullName("Updated");
        when(addressClient.update(eq(3L), any(AddressRequest.class))).thenReturn(out);

        String body = """
          {"fullName":"Updated","phone":"222","line1":"Ln","city":"City","state":"ST"}
        """;

        mvc.perform(put("/api/profile/me/addresses/3")
                .header("Authorization", AUTH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.fullName", is("Updated")));
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteAddress_ok() throws Exception {
        mvc.perform(delete("/api/profile/me/addresses/12").header("Authorization", AUTH))
           .andExpect(status().isOk());
        verify(addressClient).delete(12L);
    }

    @Test
    @WithMockUser(roles = "USER")
    void setDefaultAddress_ok() throws Exception {
        AddressResponse out = addr(4); out.setDefaultShipping(true);
        when(addressClient.setDefault(4L)).thenReturn(out);

        mvc.perform(put("/api/profile/me/addresses/4/default").header("Authorization", AUTH))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.defaultShipping", is(true)));
    }

    // ---------- ADMIN proxy endpoints ----------

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminAddress_listAll_ok() throws Exception {
        when(addressClient.adminListAll(0, 20))
            .thenReturn(new PageImpl<>(List.of(addr(1), addr(2))));

        mvc.perform(get("/api/profile/admin/addresses")
                .param("page", "0").param("size", "20"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.totalElements", is(2)))
           .andExpect(jsonPath("$.content[1].id", is(2)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminAddress_listByUser_ok() throws Exception {
        when(addressClient.adminListByUser(99L, 0, 10))
            .thenReturn(new PageImpl<>(List.of(addr(7))));

        mvc.perform(get("/api/profile/admin/users/99/addresses")
                .param("page", "0").param("size", "10"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.content[0].id", is(7)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminAddress_get_update_delete_ok() throws Exception {
        when(addressClient.adminGet(5L)).thenReturn(addr(5));
        mvc.perform(get("/api/profile/admin/addresses/5"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.id", is(5)));

        AddressResponse upd = addr(5); upd.setFullName("Admin Updated");
        when(addressClient.adminUpdate(eq(5L), any(AddressRequest.class))).thenReturn(upd);

        mvc.perform(put("/api/profile/admin/addresses/5")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                  {"fullName":"Admin Updated","phone":"1","line1":"L","city":"C","state":"S"}
                """))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.fullName", is("Admin Updated")));

        mvc.perform(delete("/api/profile/admin/addresses/5"))
           .andExpect(status().isOk());

        verify(addressClient).adminDelete(5L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminAddress_setDefault_ok() throws Exception {
        AddressResponse out = addr(9); out.setDefaultShipping(true);
        when(addressClient.adminSetDefault(9L)).thenReturn(out);

        mvc.perform(put("/api/profile/admin/addresses/9/default"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.defaultShipping", is(true)));
    }
}
