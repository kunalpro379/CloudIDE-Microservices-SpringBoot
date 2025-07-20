package com.example.sessionmanager.client;

import com.example.sessionmanager.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(name = "authentication-service", url = "${feign.client.config.authentication-service.url}")
public interface AuthenticationClient {

     @GetMapping("/validate")
     ResponseEntity<Map<String, Object>> validateToken(@RequestHeader("Authorization") String authHeader);

     @GetMapping("/user-info")
     ResponseEntity<UserDTO> getUserInfo(@RequestHeader("Authorization") String authHeader);
}