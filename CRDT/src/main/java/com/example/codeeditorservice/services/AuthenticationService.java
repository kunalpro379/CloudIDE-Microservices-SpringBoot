package com.example.codeeditorservice.services;

import com.example.codeeditorservice.Dto.AuthenticationRequestDTO;
import com.example.codeeditorservice.Dto.AuthenticationResponseDTO;
import com.example.codeeditorservice.Dto.RegisterRequestDTO;

public interface AuthenticationService {
    AuthenticationResponseDTO register(RegisterRequestDTO request);

    AuthenticationResponseDTO authenticate(AuthenticationRequestDTO request);

    void logout();
}