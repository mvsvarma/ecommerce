package com.eCommerce.userProfile.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.eCommerce.userProfile.dto.AddressRequest;
import com.eCommerce.userProfile.dto.AddressResponse;
import com.eCommerce.userProfile.dto.ShippingAddressDTO;
import com.eCommerce.userProfile.dto.UserProfileRequest;
import com.eCommerce.userProfile.dto.UserProfileResponse;
import com.eCommerce.userProfile.entity.UserProfile;
import com.eCommerce.userProfile.feign.AddressClient;
import com.eCommerce.userProfile.repository.UserProfileRepository;
import com.eCommerce.userProfile.security.JwtUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
@RestController
@RequestMapping("/api/profile")
public class UserProfileController {

    @Autowired
    private UserProfileRepository repo;

    @Autowired
    private JwtUtils jwtUtils;

    @GetMapping("/me")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<UserProfileResponse> getProfile(@RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserIdFromToken(authHeader);
        Optional<UserProfile> profile = repo.findById(userId);
        return profile.map(p -> ResponseEntity.ok(toDto(p))).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/me")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> createOrUpdateProfile(@RequestHeader("Authorization") String authHeader,
                                                   @RequestBody UserProfileRequest req) {
        Long userId = extractUserIdFromToken(authHeader);
        UserProfile profile = new UserProfile();
        profile.setId(userId);
        profile.setFullName(req.getFullName());
        profile.setAddress(req.getAddress());
        profile.setPhoneNumber(req.getPhoneNumber());
        repo.save(profile);
        return ResponseEntity.ok(toDto(profile));//Profile updated");
    }
    
 // ADMIN: Get any user profile by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<UserProfileResponse> getProfileById(@PathVariable Long id) {
        return repo.findById(id)
                   .map(p -> ResponseEntity.ok(toDto(p)))
                   .orElse(ResponseEntity.notFound().build());
    }
    
 // ADMIN: Delete a user profile
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> deleteProfile(@PathVariable Long id) {
        if (repo.existsById(id)) {
            repo.deleteById(id);
            return ResponseEntity.ok("Profile deleted");
        }
        return ResponseEntity.notFound().build();
    }

    private Long extractUserIdFromToken(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return jwtUtils.getUserIdFromJwtToken(token); // extract from claim
    }

    private UserProfileResponse toDto(UserProfile p) {
        UserProfileResponse dto = new UserProfileResponse();
        dto.setId(p.getId());
        dto.setFullName(p.getFullName());
        dto.setAddress(p.getAddress());
        dto.setPhoneNumber(p.getPhoneNumber());
        return dto;
    }
    
    @GetMapping("/all")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public List<UserProfileResponse> getAllProfiles() {
        return repo.findAll().stream()
                   .map(this::toDto)
                   .collect(Collectors.toList());
    }


    @GetMapping("/test")
    public String testPublic() { return "public"; }
    
    
    
    
    
 // inside com.eCommerce.userProfile.controller.UserProfileController

    @Autowired
    AddressClient addressClient;

    // ---------- Address subresources under /api/profile ----------

    // List my addresses
    @GetMapping("/me/addresses")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public List<AddressResponse> listMyAddresses(
            @RequestHeader("Authorization") String authHeader) {
        // Authorization header is auto-forwarded to AddressService via Feign interceptor
        return addressClient.listMyAddresses();
    }

    // Get one of my addresses
    @GetMapping("/me/addresses/{addressId}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public AddressResponse getMyAddress(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long addressId) {
        return addressClient.getMyAddress(addressId);
    }

    // Shipping snapshot (for order checkout UIs)
    @GetMapping("/me/addresses/{addressId}/shipping")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public ShippingAddressDTO getMyShippingSnapshot(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long addressId) {
        return addressClient.getShipping(addressId);
    }

    // Create address
    @PostMapping("/me/addresses")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public AddressResponse createAddress(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody @jakarta.validation.Valid com.eCommerce.userProfile.dto.AddressRequest req) {
        return addressClient.create(req);
    }

    // Update address
    @PutMapping("/me/addresses/{addressId}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public AddressResponse updateAddress(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long addressId,
            @RequestBody @jakarta.validation.Valid AddressRequest req) {
        return addressClient.update(addressId, req);
    }

    // Delete address (hard delete)
    @DeleteMapping("/me/addresses/{addressId}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> deleteAddress(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long addressId) {
        addressClient.delete(addressId);
        return ResponseEntity.ok().build();
    }

    // Make default shipping
    @PutMapping("/me/addresses/{addressId}/default")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public AddressResponse setDefaultAddress(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long addressId) {
        return addressClient.setDefault(addressId);
    }
    
    
    
    //////only admin access
 // inside com.eCommerce.userProfile.controller.UserProfileController (additions)

    
    // ----- ADMIN proxy endpoints -----
    @GetMapping("/admin/addresses")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Page<AddressResponse> adminListAllAddrs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return addressClient.adminListAll(page, size);
    }

    @GetMapping("/admin/users/{userId}/addresses")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Page<AddressResponse> adminListUserAddrs(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return addressClient.adminListByUser(userId, page, size);
    }

    @GetMapping("/admin/addresses/{addressId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public AddressResponse adminGetAddr(@PathVariable Long addressId) {
        return addressClient.adminGet(addressId);
    }

    @PostMapping("/admin/users/{userId}/addresses")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public AddressResponse adminCreateAddr(
            @PathVariable Long userId,
            @RequestBody @jakarta.validation.Valid com.eCommerce.userProfile.dto.AddressRequest req) {
        return addressClient.adminCreate(userId, req);
    }

    @PutMapping("/admin/addresses/{addressId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public AddressResponse adminUpdateAddr(
            @PathVariable Long addressId,
            @RequestBody @jakarta.validation.Valid com.eCommerce.userProfile.dto.AddressRequest req) {
        // If PATCH is flaky, you can call adminPutUpdate instead:
        return addressClient.adminUpdate(addressId, req);
    }

 
    @DeleteMapping("/admin/addresses/{addressId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> adminDeleteAddr(@PathVariable Long addressId) {
        addressClient.adminDelete(addressId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/admin/addresses/{addressId}/default")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public AddressResponse adminSetDefaultAddr(@PathVariable Long addressId) {
        return addressClient.adminSetDefault(addressId);
    }


}
