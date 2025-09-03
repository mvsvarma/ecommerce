package com.eCommerce.orderService.feign;



import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import com.eCommerce.orderService.dto.AddressResponseDTO;
import com.eCommerce.orderService.dto.ShippingAddressDTO;

@FeignClient(name = "addressService")
public interface AddressClient {

	  @GetMapping("/api/me/addresses")
      List<AddressResponseDTO> listMyAddresses();
	  @GetMapping("/api/me/addresses/{id}/shipping")
	  ShippingAddressDTO getShipping(@PathVariable("id") Long id);
	  
	  
	}
