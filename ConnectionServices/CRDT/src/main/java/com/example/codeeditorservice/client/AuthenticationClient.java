package com.example.codeeditorservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

/**
 * Client interface for communicating with the Authentication service.
 * This uses Spring Cloud Feign to create a declarative REST client.
 */
@FeignClient(name = "authentication-service", url = "${authentication.service.url}")
public interface AuthenticationClient {

     /**
      * Validate a JWT token with the Authentication service
      * 
      * @param token The JWT token to validate
      * @return A map containing the validation result and user information
      */
     @GetMapping("/validate")
     Map<String, Object> validateToken(@RequestHeader("Authorization") String token);

     /**
      * Authenticate a user with the Authentication service
      * 
      * @param request The authentication request containing username and password
      * @return A map containing the authentication result, including tokens and user
      *         information
      */
     @PostMapping("/authenticate")
     Map<String, Object> authenticate(@RequestBody Map<String, String> request);

     /**
      * Register a new user with the Authentication service
      * 
      * @param request The registration request containing user details
      * @return A map containing the registration result, including tokens and user
      *         information
      */
     @PostMapping("/register")
     Map<String, Object> register(@RequestBody Map<String, String> request);

     /**
      * Refresh an authentication token
      * 
      * @param request The refresh token request
      * @return A map containing the new tokens
      */
     @PostMapping("/refresh")
     Map<String, Object> refreshToken(@RequestBody Map<String, String> request);
}