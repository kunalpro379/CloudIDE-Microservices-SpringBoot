
package com.example.codeeditorservice.repository;

import com.example.codeeditorservice.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);

    Optional<User> findUserByUsernameOrEmail(String username, String email);
}