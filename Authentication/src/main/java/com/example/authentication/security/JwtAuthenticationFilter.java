package com.example.authentication.security;

import com.example.authentication.services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

     private final JwtService jwtService;
     private final UserDetailsService userDetailsService;

     @Override
     protected void doFilterInternal(
               HttpServletRequest request,
               HttpServletResponse response,
               FilterChain filterChain) throws ServletException, IOException {

          final String authHeader = request.getHeader("Authorization");
          final String jwt;
          final String username;

          // Skip JWT validation for certain paths
          if (shouldSkipJwtValidation(request)) {
               filterChain.doFilter(request, response);
               return;
          }

          // Extract JWT token from Authorization header
          if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
               filterChain.doFilter(request, response);
               return;
          }

          jwt = authHeader.substring(7);

          try {
               username = jwtService.extractUsername(jwt);

               // If username is found and no authentication is set in security context
               if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                    // Load user details
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    // Validate token
                    if (jwtService.isTokenValid(jwt, userDetails)) {

                         // Ensure it's an access token (not refresh token)
                         if (jwtService.isAccessToken(jwt)) {

                              // Create authentication token
                              UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities());

                              authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                              SecurityContextHolder.getContext().setAuthentication(authToken);

                              log.debug("JWT authentication successful for user: {}", username);
                         } else {
                              log.warn("Refresh token used for authentication, access denied");
                         }
                    } else {
                         log.warn("JWT token validation failed for user: {}", username);
                    }
               }
          } catch (Exception e) {
               log.error("JWT authentication error: {}", e.getMessage());
               // Clear security context in case of error
               SecurityContextHolder.clearContext();
          }

          filterChain.doFilter(request, response);
     }

     /**
      * Determines if JWT validation should be skipped for the current request
      */
     private boolean shouldSkipJwtValidation(HttpServletRequest request) {
          String path = request.getRequestURI();

          // Skip JWT validation for public endpoints
          return path.startsWith("/api/auth/") ||
                    path.startsWith("/api/health") ||
                    path.startsWith("/api/docs/") ||
                    path.startsWith("/swagger-ui/") ||
                    path.startsWith("/v3/api-docs/") ||
                    path.startsWith("/actuator/") ||
                    path.startsWith("/oauth2/") ||
                    path.startsWith("/login/oauth2/") ||
                    path.startsWith("/ws/") ||
                    path.startsWith("/socket.io/") ||
                    path.equals("/") ||
                    path.equals("/favicon.ico");
     }
}