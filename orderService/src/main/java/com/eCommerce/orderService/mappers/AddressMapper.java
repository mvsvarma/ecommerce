package com.eCommerce.orderService.mappers;
//src/main/java/com/eCommerce/orderService/mapper/AddressMapper.java

import com.eCommerce.orderService.dto.ShippingAddressDTO;
import com.eCommerce.orderService.entity.EmbeddedAddress;
import org.springframework.stereotype.Component;

@Component
public class AddressMapper {
 public EmbeddedAddress toEmbedded(ShippingAddressDTO s) {
     if (s == null) return null;
     EmbeddedAddress a = new EmbeddedAddress();
     a.setFullName(s.fullName);
     a.setPhone(s.phone);
     a.setLine1(s.line1);
     a.setCity(s.city);
     a.setState(s.state);
     a.setDefaultShipping(false); // snapshot flag
     return a;
 }
}
