package com.example.codeeditorservice.repository;

import com.example.codeeditorservice.entities.RefreshToken;
import com.example.codeeditorservice.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

     Optional<RefreshToken> findByTokenHash(String tokenHash);

     Optional<RefreshToken> findByTokenHashAndUser(String tokenHash, User user);

     @Modifying
     @Transactional
     @Query("UPDATE RefreshToken rt SET rt.revokedAt = CURRENT_TIMESTAMP WHERE rt.user.userId = :userId AND rt.revokedAt IS NULL")
     void revokeAllTokensForUser(UUID userId);

     @Modifying
     @Transactional
     @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < CURRENT_TIMESTAMP OR rt.revokedAt IS NOT NULL")
     void deleteExpiredTokens();

     @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.userId = :userId AND rt.revokedAt IS NULL AND rt.expiresAt > CURRENT_TIMESTAMP")
     Optional<RefreshToken> findValidTokenByUser(UUID userId);

     @Modifying
     @Transactional
     @Query("DELETE FROM RefreshToken rt WHERE rt.user.userId = :userId")
     void deleteAllTokensForUser(UUID userId);
}