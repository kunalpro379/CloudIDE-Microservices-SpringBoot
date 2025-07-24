package com.example.codeeditorservice.mapper;

import com.example.codeeditorservice.Dto.AuthenticationRequestDTO;
import com.example.codeeditorservice.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuthenticationDTOUserMapper {
     User authenticationRequestDTOToUser(AuthenticationRequestDTO authenticationRequestDTO);
}