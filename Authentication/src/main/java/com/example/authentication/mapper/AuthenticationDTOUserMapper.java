package com.example.authentication.mapper;

import com.example.authentication.dto.AuthenticationRequestDTO;
import com.example.authentication.entities.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthenticationDTOUserMapper {
     User authenticationRequestDTOToUser(AuthenticationRequestDTO authenticationRequestDTO);
}