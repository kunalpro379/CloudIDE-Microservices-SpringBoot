package com.example.sessionmanager.security;

import com.example.sessionmanager.client.AuthenticationClient;
import com.example.sessionmanager.dto.UserDTO;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

     private final JwtService jwtService;
     private final AuthenticationClient authenticationClient;

     @Override
     protected void doFilterInternal(
               HttpServletRequest request,
               HttpServletResponse response,
               FilterChain filterChain) throws ServletException, IOException {

          final String authHeader = request.getHeader("Authorization");
          final String jwt;
          final String username;
          final UUID userId;

          // Skip JWT validation for certain paths
          if (shouldSkipJwtValidation(request)) {
               filterChain.doFilter(request, response);
               return;
          }

          // Extract JWT token from Authorization header
          if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
               response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
               response.getWriter().write("{\"error\": \"Missing or invalid Authorization header\"}");
               return;
          }

          jwt = authHeader.substring(7);

          try {
               // Extract user information from token
               username = jwtService.extractUsername(jwt);
               userId = jwtService.extractUserIdAsUUID(jwt);

               if (username != null && userId != null
                         && SecurityContextHolder.getContext().getAuthentication() == null) {

                    // Validate token locally
                    if (jwtService.isTokenValid(jwt)) {
                         // Create UserDTO from JWT claims
                         UserDTO userInfo = UserDTO.builder()
                                   .userId(userId)
                                   .username(username)
                                   .email(jwtService.extractEmail(jwt))
                                   .build();

                         // Create authentication token
                         UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                   userInfo,
                                   null,
                                   List.of(new SimpleGrantedAuthority("ROLE_USER")));

                         authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                         SecurityContextHolder.getContext().setAuthentication(authToken);

                         // Add user ID to request headers for controllers
                         request.setAttribute("X-User-ID", userId);

                         log.debug("JWT authentication successful for user: {}", username);
                    } else {
                         log.warn("JWT token validation failed for user: {}", username);
                         response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                         response.getWriter().write("{\"error\": \"Token validation failed\"}");
                         return;
                    }
               }
          } catch (Exception e) {
               log.error("JWT authentication error: {}", e.getMessage());
               response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
               response.getWriter().write("{\"error\": \"Authentication failed\"}");
               return;
          }

          filterChain.doFilter(request, response);
     }

     /**
      * Determines if JWT validation should be skipped for the current request
      */
     private boolean shouldSkipJwtValidation(HttpServletRequest request) {
          String path = request.getRequestURI();

          // Skip JWT validation for public endpoints
          return path.startsWith("/api/session/health") ||
                    path.startsWith("/api/session/docs/") ||
                    path.startsWith("/swagger-ui/") ||
                    path.startsWith("/v3/api-docs/") ||
                    path.startsWith("/actuator/") ||
                    path.equals("/") ||
                    path.equals("/favicon.ico");
     }
}