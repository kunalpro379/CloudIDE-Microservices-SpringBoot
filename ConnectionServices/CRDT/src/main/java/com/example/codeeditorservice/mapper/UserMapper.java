package com.example.codeeditorservice.mapper;
import org.mapstruct.Mapper;

import com.example.codeeditorservice.dt.UserDTO;
import com.example.codeeditorservice.entities.User;

@Mappper(componentModel = "spring")
public interface UserMapper {
    UserDTO toDto(User user);
    User toEntity(UserDTO userDTO);
}