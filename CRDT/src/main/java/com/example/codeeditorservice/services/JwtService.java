package com.example.codeeditorservice.services;

import com.example.codeeditorservice.entities.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {
    @Value("${JWT_SECRET_KEY}")
    private String secretKey;
    private JwtParser jwtParser;

    @Autowired
    private UserDetailsService userDetailsService;

    @PostConstruct
    public void initJwtService() {
        jwtParser = Jwts.parserBuilder().setSigningKey(getSignKey()).build();
    }

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Claims extractAllClaims(String token) throws JwtException {
        try {
            return jwtParser.parseClaimsJws(token).getBody();
        } catch (JwtException e) {
            throw new JwtException("Invalid JWT token", e);
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String generateToken(String username) {
        return generateToken(new HashMap<>(), username);
    }

    public String generateToken(Map<String, Object> extraClaims, String username) {
        int keyDuration = 60 * 60 * 1000; // 1 hour
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + keyDuration))
                .signWith(getSignKey())
                .compact();
    }

    public boolean validateUserAndToken(String token) {
        String username = extractUsername(token);
        if (username == null) {
            return false;
        }
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return isTokenValid(token, userDetails);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}