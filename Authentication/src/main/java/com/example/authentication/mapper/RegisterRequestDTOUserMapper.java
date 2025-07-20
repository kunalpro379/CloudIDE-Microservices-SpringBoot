package com.example.authentication.mapper;

import com.example.authentication.dto.RegisterRequestDTO;
import com.example.authentication.entities.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RegisterRequestDTOUserMapper {
     User registerRequestDTOToUser(RegisterRequestDTO registerRequestDTO);
}