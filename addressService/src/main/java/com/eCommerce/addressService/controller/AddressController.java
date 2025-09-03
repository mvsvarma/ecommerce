package com.eCommerce.addressService.controller;


import com.eCommerce.addressService.dto.AddressRequest;
import com.eCommerce.addressService.dto.AddressResponse;
import com.eCommerce.addressService.dto.ShippingAddressDTO;
import com.eCommerce.addressService.entity.Address;
import com.eCommerce.addressService.repository.AddressRepository;
import com.eCommerce.addressService.security.JwtUtils;
import com.eCommerce.addressService.service.AddressManager;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import org.springframework.data.domain.Page;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AddressController {

    private final AddressManager svc;
    private final JwtUtils jwtUtils;
    private final AddressRepository repo;

    public AddressController(AddressManager svc, JwtUtils jwtUtils, AddressRepository repo) {
        this.svc = svc; this.jwtUtils = jwtUtils; this.repo = repo;
    }

    private Long uid(String auth) {
        String token = auth.replace("Bearer ", "");
        return jwtUtils.getUserIdFromJwtToken(token);
    }

    // ---- USER endpoints ----
    @GetMapping("/me/addresses")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public List<AddressResponse> listMine(@RequestHeader("Authorization") String auth) {
        return svc.listMine(uid(auth));
    }

    @GetMapping("/me/addresses/{id}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public AddressResponse getMine(@RequestHeader("Authorization") String auth, @PathVariable Long id) {
        return svc.getMine(uid(auth), id);
    }

    // for OrderService Feign: returns only shipping fields
    @GetMapping("/me/addresses/{id}/shipping")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public ShippingAddressDTO getShipping(@RequestHeader("Authorization") String auth, @PathVariable Long id) {
        Long userId = uid(auth);
        Address a = repo.findByIdAndUserId(id, userId).orElseThrow(() -> new RuntimeException("Address not found"));
        return svc.toShipping(a);
    }

    @PostMapping("/me/addresses")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public AddressResponse create(@RequestHeader("Authorization") String auth, @Valid @RequestBody AddressRequest req) {
        return svc.create(uid(auth), req);
    }

    @PutMapping("/me/addresses/{id}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public AddressResponse update(@RequestHeader("Authorization") String auth, @PathVariable Long id, @Valid @RequestBody AddressRequest req) {
        return svc.update(uid(auth), id, req);
    }

    @DeleteMapping("/me/addresses/{id}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> delete(@RequestHeader("Authorization") String auth, @PathVariable Long id) {
        svc.delete(uid(auth), id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/me/addresses/{id}/default")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public AddressResponse setDefault(@RequestHeader("Authorization") String auth, @PathVariable Long id) {
        return svc.setDefault(uid(auth), id);
    }
    
    
    //////////admin controllers
    
    
 // com.eCommerce.addressService.controller.AddressController (additions)

    

    @GetMapping("/admin/addresses")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Page<AddressResponse> adminListAll(@RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "20") int size) {
        return svc.adminListAll(page, size);
    }

    @GetMapping("/admin/users/{userId}/addresses")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Page<AddressResponse> adminListByUser(@PathVariable Long userId,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "20") int size) {
        return svc.adminListByUser(userId, page, size);
    }

    @GetMapping("/admin/addresses/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public AddressResponse adminGet(@PathVariable Long id) {
        return svc.adminGet(id);
    }

    @PostMapping(value = "/admin/users/{userId}/addresses")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public AddressResponse adminCreate(@PathVariable Long userId,
                                       @RequestBody @jakarta.validation.Valid AddressRequest req) {
        return svc.adminCreate(userId, req);
    }

    @PutMapping(value = "/admin/addresses/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public AddressResponse adminUpdate(@PathVariable Long id,
                                       @RequestBody @Valid AddressRequest req) {
        return svc.adminUpdate(id, req);
    }


    @DeleteMapping("/admin/addresses/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> adminDelete(@PathVariable Long id) {
        svc.adminDelete(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/admin/addresses/{id}/default")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public AddressResponse adminSetDefault(@PathVariable Long id) {
        return svc.adminSetDefault(id);
    }

}
