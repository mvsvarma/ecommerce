package com.eCommerce.userProfile.feign;
//com.eCommerce.userProfile.feign.AddressClient.java

import com.eCommerce.userProfile.dto.AddressRequest;
import com.eCommerce.userProfile.dto.AddressResponse;
import com.eCommerce.userProfile.dto.ShippingAddressDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
name = "addressService"
)
public interface AddressClient {

@GetMapping("/api/me/addresses")
List<AddressResponse> listMyAddresses();

@GetMapping("/api/me/addresses/{id}")
AddressResponse getMyAddress(@PathVariable("id") Long id);

@GetMapping("/api/me/addresses/{id}/shipping")
ShippingAddressDTO getShipping(@PathVariable("id") Long id);

@PostMapping("/api/me/addresses")
AddressResponse create(@RequestBody AddressRequest req);

@PutMapping("/api/me/addresses/{id}")
AddressResponse update(@PathVariable("id") Long id, @RequestBody AddressRequest req);

@DeleteMapping("/api/me/addresses/{id}")
void delete(@PathVariable("id") Long id);

@PutMapping("/api/me/addresses/{id}/default")
AddressResponse setDefault(@PathVariable("id") Long id);



///ADMINS

@GetMapping("/api/admin/addresses")
Page<AddressResponse> adminListAll(@RequestParam("page") int page,
                                   @RequestParam("size") int size);

@GetMapping("/api/admin/users/{userId}/addresses")
Page<AddressResponse> adminListByUser(@PathVariable("userId") Long userId,
                                      @RequestParam("page") int page,
                                      @RequestParam("size") int size);

@GetMapping("/api/admin/addresses/{id}")
AddressResponse adminGet(@PathVariable("id") Long id);

@PostMapping(value="/api/admin/users/{userId}/addresses", consumes="application/json")
AddressResponse adminCreate(@PathVariable("userId") Long userId,
                            @RequestBody AddressRequest req);

@PutMapping(value="/api/admin/addresses/{id}", consumes="application/json")
AddressResponse adminUpdate(@PathVariable("id") Long id,
                            @RequestBody AddressRequest req);



@DeleteMapping("/api/admin/addresses/{id}")
void adminDelete(@PathVariable("id") Long id);

@PutMapping("/api/admin/addresses/{id}/default")
AddressResponse adminSetDefault(@PathVariable("id") Long id);
}
