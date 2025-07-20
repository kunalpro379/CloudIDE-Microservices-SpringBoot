package com.example.authentication.services;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import com.example.authentication.entities.User;
import com.example.authentication.entities.RefreshToken;
import com.example.authentication.repositories.RefreshTokenRepository;

import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {

     @Value("${jwt.secret}")
     private String secretKey;

     @Value("${jwt.access-token-expiration}")
     private Long accessTokenExpiration;

     @Value("${jwt.refresh-token-expiration}")
     private Long refreshTokenExpiration;

     private final RefreshTokenRepository refreshTokenRepository;

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

     public String generateAccessToken(User user) {
          return generateAccessToken(new HashMap<>(), user);
     }

     public String generateAccessToken(Map<String, Object> extraClaims, User user) {
          extraClaims.put("userId", user.getUserId().toString());
          extraClaims.put("email", user.getEmail());
          extraClaims.put("type", "access");

          return buildToken(extraClaims, user.getUsername(), accessTokenExpiration);
     }

     public String generateRefreshToken(User user) {
          Map<String, Object> claims = new HashMap<>();
          claims.put("userId", user.getUserId().toString());
          claims.put("type", "refresh");

          return buildToken(claims, user.getUsername(), refreshTokenExpiration);
     }

     private String buildToken(Map<String, Object> extraClaims, String subject, Long expiration) {
          Date now = new Date();
          Date expiryDate = new Date(now.getTime() + expiration);

          return Jwts.builder()
                    .setClaims(extraClaims)
                    .setSubject(subject)
                    .setIssuedAt(now)
                    .setExpiration(expiryDate)
                    .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                    .compact();
     }

     public boolean isTokenValid(String token, UserDetails userDetails) {
          try {
               final String username = extractUsername(token);
               return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
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

     public RefreshToken storeRefreshToken(String token, User user, String deviceInfo) {
          RefreshToken refreshToken = RefreshToken.builder()
                    .tokenHash(hashToken(token))
                    .user(user)
                    .expiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000))
                    .deviceInfo(deviceInfo)
                    .build();

          return refreshTokenRepository.save(refreshToken);
     }

     public boolean validateRefreshToken(String token, User user) {
          String hashedToken = hashToken(token);
          return refreshTokenRepository.findByTokenHashAndUser(hashedToken, user)
                    .map(RefreshToken::isValid)
                    .orElse(false);
     }

     public void revokeRefreshToken(String token) {
          String hashedToken = hashToken(token);
          refreshTokenRepository.findByTokenHash(hashedToken)
                    .ifPresent(refreshToken -> {
                         refreshToken.revoke();
                         refreshTokenRepository.save(refreshToken);
                    });
     }

     public void revokeAllRefreshTokensForUser(User user) {
          refreshTokenRepository.revokeAllTokensForUser(user.getUserId());
     }

     public void cleanupExpiredTokens() {
          refreshTokenRepository.deleteExpiredTokens();
     }

     private String hashToken(String token) {
          // Simple hash for storage - in production use proper hashing
          return String.valueOf(token.hashCode());
     }

     public Map<String, Object> createTokenResponse(String accessToken, String refreshToken) {
          Map<String, Object> response = new HashMap<>();
          response.put("access_token", accessToken);
          response.put("refresh_token", refreshToken);
          response.put("token_type", "Bearer");
          response.put("expires_in", accessTokenExpiration / 1000);
          return response;
     }
}