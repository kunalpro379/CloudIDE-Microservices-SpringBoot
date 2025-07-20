package org.example.codeeditorservice.repository;

import lombok.RequiredArgsConstructor;
import org.cce.backend.entity.User;
import org.cce.backend.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
@Configuration
@RequiredArgsConstructor
public class AppConfig{
    private final UserRepository userRepository;
    @Bean
    public UserDetailsService userDetailsService(){
        return (username)->{
            retrurn userRepository.findByUsername(username).orElseThrow(()->new UsernameNotFoundException("User not found"));
        }
    }

    //Creaitng a DaoAuthenticatorProvider bean which will tell the Sprong to use the database via UserDetaiels Service to load users
    //Encrypt Decrpt the passwd
    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider authprovider =new DaoAuthenticationProvider();
        authprovider.setUserDetailsService(userDetailsService());
        authprovider.setPasswordEncoder(passwordEncoder());
        return authprovider;
    }
    @Bean
    public AuthenticationManager authenticationmanager(AuthenticationConfiguration configuration) throws Exception{
        return configuration.getAuthenticationManager();
    }


    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}