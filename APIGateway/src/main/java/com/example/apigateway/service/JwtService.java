package com.example.apigateway.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Service
@Slf4j
public class JwtService {

     @Value("${jwt.secret}")
     private String secretKey;

     @Value("${jwt.access-token-expiration}")
     private Long accessTokenExpiration;

     private Key getSigningKey() {
          byte[] keyBytes = Decoders.BASE64.decode(secretKey);
          return Keys.hmacShaKeyFor(keyBytes);
     }

     public String extractUsername(String token) {
          return extractClaim(token, Claims::getSubject);
     }

     public String extractUserId(String token) {
          return extractClaim(token, claims -> claims.get("userId", String.class));
     }

     public String extractEmail(String token) {
          return extractClaim(token, claims -> claims.get("email", String.class));
     }

     public Date extractExpiration(String token) {
          return extractClaim(token, Claims::getExpiration);
     }

     public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
          final Claims claims = extractAllClaims(token);
          return claimsResolver.apply(claims);
     }

     private Claims extractAllClaims(String token) {
          try {
               return Jwts.parserBuilder()
                         .setSigningKey(getSigningKey())
                         .build()
                         .parseClaimsJws(token)
                         .getBody();
          } catch (JwtException e) {
               log.error("Invalid JWT token: {}", e.getMessage());
               throw new JwtException("Invalid JWT token", e);
          }
     }

     public boolean isTokenValid(String token) {
          try {
               final String username = extractUsername(token);
               return username != null && !isTokenExpired(token) && isAccessToken(token);
          } catch (Exception e) {
               log.error("Token validation failed: {}", e.getMessage());
               return false;
          }
     }

     public boolean isTokenExpired(String token) {
          return extractExpiration(token).before(new Date());
     }

     public String getTokenType(String token) {
          return extractClaim(token, claims -> claims.get("type", String.class));
     }

     public boolean isAccessToken(String token) {
          return "access".equals(getTokenType(token));
     }

     public boolean isRefreshToken(String token) {
          return "refresh".equals(getTokenType(token));
     }

     public String extractToken(String authHeader) {
          if (authHeader != null && authHeader.startsWith("Bearer ")) {
               return authHeader.substring(7);
          }
          return null;
     }
}