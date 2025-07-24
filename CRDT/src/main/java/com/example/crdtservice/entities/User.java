package com.example.codeeditorservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;
import com.example.codeeditorservice.enums.Role;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Table(name = "users")
@Entity
public class User implements UserDetails {
    @Id
    private String username;
    private String password;
    @Column(unique = true)
    private String email;
    private Role role;
    @OneToMany(mappedBy = "user")
    private List<UserDocs> accessDocs;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
