package com.example.authentication.repositories;

import com.example.authentication.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

     Optional<User> findByUsername(String username);

     Optional<User> findByEmail(String email);

     Optional<User> findByUsernameOrEmail(String username, String email);

     boolean existsByUsername(String username);

     boolean existsByEmail(String email);

     @Query("SELECT u FROM User u WHERE u.oauthProvider = :provider AND u.oauthProviderId = :providerId")
     Optional<User> findByOAuthProviderAndProviderId(String provider, String providerId);

     @Query("SELECT u FROM User u WHERE u.isActive = true")
     Optional<User> findActiveUsers();
}