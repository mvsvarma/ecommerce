package com.eCommerce.cartItemService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;
@EnableTransactionManagement
@SpringBootApplication
@EnableFeignClients(basePackages = "com.eCommerce.cartItemService.feign")
public class CartItemServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CartItemServiceApplication.class, args);
	}

}
