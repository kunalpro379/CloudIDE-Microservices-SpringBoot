package com.example.codeeditorservice.mapper;

import com.example.codeeditorservice.Dto.UserDTO;
import com.example.codeeditorservice.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    UserDTO toDto(User user);

    User toEntity(UserDTO userDTO);
}