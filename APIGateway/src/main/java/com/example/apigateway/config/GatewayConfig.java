package com.example.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class GatewayConfig {

     @Bean
     @Primary
     public ReactiveRedisTemplate<String, String> reactiveRedisTemplate(ReactiveRedisConnectionFactory factory) {
          StringRedisSerializer serializer = new StringRedisSerializer();
          RedisSerializationContext<String, String> context = RedisSerializationContext
                    .<String, String>newSerializationContext(serializer)
                    .value(serializer)
                    .build();
          return new ReactiveRedisTemplate<>(factory, context);
     }

     @Bean
     public CorsWebFilter corsWebFilter() {
          CorsConfiguration corsConfig = new CorsConfiguration();
          corsConfig.setAllowedOrigins(Arrays.asList(
                    "http://localhost:3000",
                    "http://localhost:8233",
                    "http://localhost:8081",
                    "http://localhost:8082"));
          corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
          corsConfig.setAllowedHeaders(Arrays.asList("*"));
          corsConfig.setAllowCredentials(true);
          corsConfig.setMaxAge(3600L);

          UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
          source.registerCorsConfiguration("/**", corsConfig);

          return new CorsWebFilter(source);
     }
}