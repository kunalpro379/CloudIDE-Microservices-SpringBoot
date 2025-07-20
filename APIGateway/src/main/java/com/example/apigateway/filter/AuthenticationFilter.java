package com.example.apigateway.filter;

import com.example.apigateway.service.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

     @Autowired
     private JwtService jwtService;

     public AuthenticationFilter() {
          super(Config.class);
     }

     @Override
     public GatewayFilter apply(Config config) {
          return ((exchange, chain) -> {
               ServerHttpRequest request = exchange.getRequest();

               // Skip authentication for public endpoints
               if (isPublicEndpoint(request.getURI().getPath())) {
                    log.debug("Skipping authentication for public endpoint: {}", request.getURI().getPath());
                    return chain.filter(exchange);
               }

               // Extract Authorization header
               String authHeader = request.getHeaders().getFirst("Authorization");

               if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    log.warn("Missing or invalid Authorization header for path: {}", request.getURI().getPath());
                    return unauthorizedResponse(exchange, "Missing or invalid Authorization header");
               }

               // Extract and validate JWT token
               String token = jwtService.extractToken(authHeader);

               if (token == null || !jwtService.isTokenValid(token)) {
                    log.warn("Invalid JWT token for path: {}", request.getURI().getPath());
                    return unauthorizedResponse(exchange, "Invalid or expired JWT token");
               }

               // Extract user information from token and add to headers
               try {
                    String userId = jwtService.extractUserId(token);
                    String username = jwtService.extractUsername(token);
                    String email = jwtService.extractEmail(token);

                    // Add user info to request headers for downstream services
                    ServerHttpRequest modifiedRequest = request.mutate()
                              .header("X-User-Id", userId)
                              .header("X-Username", username)
                              .header("X-User-Email", email)
                              .build();

                    ServerWebExchange modifiedExchange = exchange.mutate().request(modifiedRequest).build();

                    log.debug("Authentication successful for user: {} accessing path: {}", username,
                              request.getURI().getPath());
                    return chain.filter(modifiedExchange);

               } catch (Exception e) {
                    log.error("Error processing JWT token: {}", e.getMessage());
                    return unauthorizedResponse(exchange, "Error processing authentication token");
               }
          });
     }

     private boolean isPublicEndpoint(String path) {
          List<String> publicPaths = Arrays.asList(
                    "/api/auth/register",
                    "/api/auth/authenticate",
                    "/api/auth/refresh",
                    "/api/auth/oauth",
                    "/api/auth/oauth2",
                    "/actuator/health",
                    "/actuator/info");

          return publicPaths.stream().anyMatch(path::startsWith);
     }

     private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
          ServerHttpResponse response = exchange.getResponse();
          response.setStatusCode(HttpStatus.UNAUTHORIZED);
          response.getHeaders().add("Content-Type", "application/json");

          String body = String.format("{\"error\": \"Unauthorized\", \"message\": \"%s\"}", message);
          DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));

          return response.writeWith(Mono.just(buffer));
     }

     public static class Config {
          // Configuration properties can be added here if needed
     }
}