package com.example.codeeditorservice.services;

import com.example.codeeditorservice.Dto.AuthenticationRequestDTO;
import com.example.codeeditorservice.Dto.AuthenticationResponseDTO;
import com.example.codeeditorservice.Dto.RegisterRequestDTO;
import com.example.codeeditorservice.entities.User;
import com.example.codeeditorservice.enums.Role;
import com.example.codeeditorservice.exception.UserAlreadyExistsException;
import com.example.codeeditorservice.repository.UserRepository;
import com.example.codeeditorservice.mapper.RegisterRequestDTOUserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RegisterRequestDTOUserMapper registerRequestDTOUserMapper;
    // private final JwtService jwtService; // Uncomment and fix if JwtService is
    // available

    @Override
    public AuthenticationResponseDTO register(RegisterRequestDTO request) {
        User user = registerRequestDTOUserMapper.registerRequestDTOToUser(request);
        user.setRole(Role.USER);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        validateUserNotExists(request);
        // String jwtToken = jwtService.generateToken(user.getUsername());
        // return AuthenticationResponseDTO.builder().token(jwtToken).build();
        return AuthenticationResponseDTO.builder().token("dummy-token").build();
    }

    private void validateUserNotExists(RegisterRequestDTO request) {
        userRepository.findUserByUsernameOrEmail(request.getUsername(), request.getEmail())
                .ifPresent(item -> {
                    throw new UserAlreadyExistsException("User with username " + item.getUsername() + " or email "
                            + item.getEmail() + " already exists.");
                });
    }

    @Override
    public AuthenticationResponseDTO authenticate(AuthenticationRequestDTO request) {
        // Implement authentication logic
        return AuthenticationResponseDTO.builder().token("dummy-token").build();
    }

    @Override
    public void logout() {
        // Implement logout logic
    }
}