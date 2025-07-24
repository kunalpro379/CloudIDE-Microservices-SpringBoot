package com.example.codeeditorservice.controllers;

import com.example.codeeditorservice.Dto.AuthenticationRequestDTO;
import com.example.codeeditorservice.Dto.AuthenticationResponseDTO;
import com.example.codeeditorservice.Dto.RegisterRequestDTO;
import com.example.codeeditorservice.services.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponseDTO> register(@RequestBody RegisterRequestDTO registerRequestDTO) {
        AuthenticationResponseDTO token = authenticationService.register(registerRequestDTO);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponseDTO> authenticateUser(@RequestBody AuthenticationRequestDTO request) {
        AuthenticationResponseDTO token = authenticationService.authenticate(request);
        return ResponseEntity.ok(token);
    }

    @GetMapping("/logout")
    public ResponseEntity<Void> logout() {
        authenticationService.logout();
        return ResponseEntity.ok().build();
    }
}