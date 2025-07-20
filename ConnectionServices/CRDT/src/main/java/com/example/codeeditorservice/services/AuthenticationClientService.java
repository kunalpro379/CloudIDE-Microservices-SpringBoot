package com.example.codeeditorservice.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Service to communicate with the Authentication service.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationClientService {

     private final RestTemplate restTemplate;

     @Value("${authentication.service.url}")
     private String authServiceUrl;

     /**
      * Validates a JWT token with the Authentication service.
      *
      * @param token The JWT token to validate
      * @return true if the token is valid, false otherwise
      */
     public boolean validateToken(String token) {
          try {
               HttpHeaders headers = new HttpHeaders();
               headers.set("Authorization", token);
               HttpEntity<Void> entity = new HttpEntity<>(headers);

               ResponseEntity<Map> response = restTemplate.exchange(
                         authServiceUrl + "/validate",
                         HttpMethod.GET,
                         entity,
                         Map.class);

               return response.getStatusCode().is2xxSuccessful() &&
                         response.getBody() != null &&
                         Boolean.TRUE.equals(response.getBody().get("valid"));
          } catch (Exception e) {
               log.error("Error validating token with Authentication service", e);
               return false;
          }
     }

     /**
      * Gets user information from a valid JWT token.
      *
      * @param token The JWT token
      * @return Map containing user information or null if the token is invalid
      */
     public Map<String, Object> getUserInfo(String token) {
          try {
               HttpHeaders headers = new HttpHeaders();
               headers.set("Authorization", token);
               HttpEntity<Void> entity = new HttpEntity<>(headers);

               ResponseEntity<Map> response = restTemplate.exchange(
                         authServiceUrl + "/user-info",
                         HttpMethod.GET,
                         entity,
                         Map.class);

               if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    return response.getBody();
               }
               return null;
          } catch (Exception e) {
               log.error("Error getting user info from Authentication service", e);
               return null;
          }
     }
}