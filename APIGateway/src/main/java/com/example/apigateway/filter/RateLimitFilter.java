package com.example.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Component
@Slf4j
public class RateLimitFilter extends AbstractGatewayFilterFactory<RateLimitFilter.Config> {

     private final ReactiveRedisTemplate<String, String> redisTemplate;

     @Value("${rate-limit.replenish-rate:10}")
     private int replenishRate;

     @Value("${rate-limit.burst-capacity:20}")
     private int burstCapacity;

     public RateLimitFilter(ReactiveRedisTemplate<String, String> redisTemplate) {
          super(Config.class);
          this.redisTemplate = redisTemplate;
     }

     @Override
     public GatewayFilter apply(Config config) {
          return (exchange, chain) -> {
               String key = getKey(exchange);

               return isAllowed(key)
                         .flatMap(allowed -> {
                              if (allowed) {
                                   return chain.filter(exchange);
                              } else {
                                   return rateLimitExceededResponse(exchange);
                              }
                         });
          };
     }

     private String getKey(ServerWebExchange exchange) {
          // Use IP address as the key for rate limiting
          String clientIp = exchange.getRequest().getRemoteAddress() != null
                    ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                    : "unknown";
          return "rate_limit:" + clientIp;
     }

     private Mono<Boolean> isAllowed(String key) {
          return redisTemplate.opsForValue()
                    .get(key)
                    .cast(String.class)
                    .map(Integer::parseInt)
                    .defaultIfEmpty(0)
                    .flatMap(currentCount -> {
                         if (currentCount >= burstCapacity) {
                              return Mono.just(false);
                         } else {
                              return redisTemplate.opsForValue()
                                        .increment(key)
                                        .flatMap(newCount -> {
                                             if (newCount == 1) {
                                                  // Set expiration for the key
                                                  return redisTemplate.expire(key, Duration.ofSeconds(60))
                                                            .then(Mono.just(true));
                                             }
                                             return Mono.just(true);
                                        });
                         }
                    })
                    .onErrorReturn(true); // Allow request if Redis is unavailable
     }

     private Mono<Void> rateLimitExceededResponse(ServerWebExchange exchange) {
          ServerHttpResponse response = exchange.getResponse();
          response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
          response.getHeaders().add("Content-Type", "application/json");

          String body = "{"
                    + "\"error\": \"Rate Limit Exceeded\","
                    + "\"message\": \"Too many requests. Please try again later.\","
                    + "\"timestamp\": " + System.currentTimeMillis()
                    + "}";

          DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));

          log.warn("Rate limit exceeded for request: {}", exchange.getRequest().getPath());
          return response.writeWith(Mono.just(buffer));
     }

     public static class Config {
          // Configuration properties can be added here if needed
     }
}