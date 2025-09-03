package com.eCommerce.addressService;

import com.eCommerce.addressService.controller.AddressController;
import com.eCommerce.addressService.dto.AddressRequest;
import com.eCommerce.addressService.dto.AddressResponse;
import com.eCommerce.addressService.dto.ShippingAddressDTO;
import com.eCommerce.addressService.entity.Address;
import com.eCommerce.addressService.repository.AddressRepository;
import com.eCommerce.addressService.security.JwtUtils;
import com.eCommerce.addressService.service.AddressManager;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AddressController.class)
@AutoConfigureMockMvc(addFilters = false) // disable security filter; we still use @WithMockUser for @PreAuthorize
@EnableMethodSecurity
class AddressControllerTest {

    @Autowired private MockMvc mvc;

    @MockBean private AddressManager svc;
    @MockBean private JwtUtils jwtUtils;
    @MockBean private AddressRepository repo;

    private static final String AUTH = "Bearer testtoken";
    private static final long USER = 99L;

    private AddressResponse dto(long id) {
        AddressResponse d = new AddressResponse();
        d.setId(id); d.setUserId(USER);
        d.setFullName("Alice"); d.setPhone("111");
        d.setLine1("Main"); d.setCity("Pune"); d.setState("MH");
        d.setDefaultShipping(false);
        d.setCreatedAt(LocalDateTime.now());
        d.setUpdatedAt(LocalDateTime.now());
        return d;
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void listMine_ok() throws Exception {
        when(jwtUtils.getUserIdFromJwtToken("testtoken")).thenReturn(USER);
        when(svc.listMine(USER)).thenReturn(List.of(dto(1), dto(2)));

        mvc.perform(get("/api/me/addresses").header("Authorization", AUTH))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$", hasSize(2)))
           .andExpect(jsonPath("$[0].id", is(1)));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void getMine_ok() throws Exception {
        when(jwtUtils.getUserIdFromJwtToken("testtoken")).thenReturn(USER);
        when(svc.getMine(USER, 1L)).thenReturn(dto(1));

        mvc.perform(get("/api/me/addresses/1").header("Authorization", AUTH))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void shipping_ok() throws Exception {
        when(jwtUtils.getUserIdFromJwtToken("testtoken")).thenReturn(USER);

        Address entity = new Address();
        entity.setId(1L); entity.setUserId(USER);
        entity.setFullName("Alice"); entity.setLine1("Main");
        entity.setCity("Pune"); entity.setState("MH"); entity.setPhone("111");
        when(repo.findByIdAndUserId(1L, USER)).thenReturn(Optional.of(entity));

        ShippingAddressDTO snap = new ShippingAddressDTO();
        snap.setFullName("Alice"); snap.setLine1("Main");
        snap.setCity("Pune"); snap.setState("MH"); snap.setPhone("111");
        when(svc.toShipping(entity)).thenReturn(snap);

        mvc.perform(get("/api/me/addresses/1/shipping").header("Authorization", AUTH))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.fullName", is("Alice")))
           .andExpect(jsonPath("$.line1", is("Main")));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void create_ok() throws Exception {
        when(jwtUtils.getUserIdFromJwtToken("testtoken")).thenReturn(USER);
        AddressResponse out = dto(10);
        when(svc.create(eq(USER), org.mockito.ArgumentMatchers.any(AddressRequest.class))).thenReturn(out);

        String body = """
          {
            "fullName": "Alice",
            "phone": "111",
            "line1": "Main",
            "city": "Pune",
            "state": "MH",
            "defaultShipping": true
          }
          """;

        mvc.perform(post("/api/me/addresses")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", AUTH)
                .content(body))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.id", is(10)));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void setDefault_ok() throws Exception {
        when(jwtUtils.getUserIdFromJwtToken("testtoken")).thenReturn(USER);
        AddressResponse out = dto(1);
        out.setDefaultShipping(true);
        when(svc.setDefault(USER, 1L)).thenReturn(out);

        mvc.perform(put("/api/me/addresses/1/default").header("Authorization", AUTH))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.defaultShipping", is(true)));
    }

    // ---------- ADMIN endpoints ----------

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void adminListAll_ok() throws Exception {
        when(svc.adminListAll(0, 20)).thenReturn(new PageImpl<>(List.of(dto(1), dto(2))));

        mvc.perform(get("/api/admin/addresses")
                .param("page", "0").param("size", "20"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.content[0].id", is(1)))
           .andExpect(jsonPath("$.totalElements", is(2)));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void adminCreate_ok() throws Exception {
        AddressResponse out = dto(77);
        when(svc.adminCreate(eq(42L), org.mockito.ArgumentMatchers.any(AddressRequest.class))).thenReturn(out);

        String body = """
          {
            "fullName": "Admin Add",
            "phone": "123",
            "line1": "Main",
            "city": "Pune",
            "state": "MH",
            "defaultShipping": true
          }
          """;

        mvc.perform(post("/api/admin/users/42/addresses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.id", is(77)));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void adminUpdate_ok() throws Exception {
        AddressResponse out = dto(5);
        out.setFullName("Updated");
        when(svc.adminUpdate(eq(5L), org.mockito.ArgumentMatchers.any(AddressRequest.class))).thenReturn(out);

        String body = """
          {"fullName":"Updated","phone":"777","line1":"Ln","city":"City","state":"ST"}
          """;

        mvc.perform(put("/api/admin/addresses/5")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.fullName", is("Updated")));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void adminDelete_ok() throws Exception {
        mvc.perform(delete("/api/admin/addresses/9"))
           .andExpect(status().isOk());
        Mockito.verify(svc).adminDelete(9L);
    }
}
