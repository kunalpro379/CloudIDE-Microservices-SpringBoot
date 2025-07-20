package com.example.authentication.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

     private final JwtAuthenticationFilter jwtAuthenticationFilter;
     private final AuthenticationProvider authenticationProvider;
     private final ClientRegistrationRepository clientRegistrationRepository;

     @Bean
     public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
          http
                    .csrf(AbstractHttpConfigurer::disable)
                    .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                    .authorizeHttpRequests(auth -> auth
                              // Public endpoints - be more specific
                              .requestMatchers("/register").permitAll()
                              .requestMatchers("/authenticate").permitAll()
                              .requestMatchers("/refresh").permitAll()
                              .requestMatchers("/validate").permitAll()
                              .requestMatchers("/api/health").permitAll()
                              .requestMatchers("/api/docs/**").permitAll()
                              .requestMatchers("/swagger-ui/**").permitAll()
                              .requestMatchers("/v3/api-docs/**").permitAll()
                              .requestMatchers("/actuator/**").permitAll()

                              // OAuth endpoints
                              .requestMatchers("/oauth2/**").permitAll()
                              .requestMatchers("/login/oauth2/**").permitAll()

                              // All other requests need authentication
                              .anyRequest().authenticated())
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authenticationProvider(authenticationProvider)
                    .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                    // OAuth2 Login Configuration
                    .oauth2Login(oauth2 -> oauth2
                              .clientRegistrationRepository(clientRegistrationRepository)
                              .defaultSuccessUrl("/api/auth/oauth/success")
                              .failureUrl("/api/auth/oauth/failure")
                              .permitAll());

          return http.build();
     }

     @Bean
     public CorsConfigurationSource corsConfigurationSource() {
          CorsConfiguration configuration = new CorsConfiguration();

          // Allow specific origins
          configuration.setAllowedOrigins(Arrays.asList(
                    "http://localhost:3000",
                    "http://localhost:8233",
                    "https://yourapp.com"));

          // Allow all HTTP methods
          configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

          // Allow all headers
          configuration.setAllowedHeaders(Arrays.asList("*"));

          // Allow credentials
          configuration.setAllowCredentials(true);

          // Expose headers
          configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));

          UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
          source.registerCorsConfiguration("/**", configuration);

          return source;
     }
}