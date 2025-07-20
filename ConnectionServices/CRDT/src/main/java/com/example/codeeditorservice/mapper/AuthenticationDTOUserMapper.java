package com.example.codeeditorservice.mapper;
import com.example.codeeditorservice.Dto.AuthenticationDTO;
import com.example.codeeditorservice.entities.User;
import org.mapstruct.Mapper;
@Mapper(componentModel = "spring")
public interface AuthenticationDTOUserMapper  {
     User AuthenticationRequestDTOToUser(AuthenticationRequestDTO registerRequestDTO)
}