package com.banking.accountservice.client;

import com.banking.accountservice.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/*@FeignClient(
    name = "user-service",
    url = "${user-service.url}"     // reads from application.yml
)*/

@FeignClient(name = "services")
// "services" = exact spring.application.name of User Service
// No more url = "${user-service.url}" — Eureka resolves it now!
public interface UserServiceClient {

    @GetMapping("/api/users/{id}")
    UserResponse getUserById(@PathVariable Long id);
    // Feign generates the actual HTTP GET call
    // If user not found, User Service returns 404
    // Feign throws FeignException automatically
}