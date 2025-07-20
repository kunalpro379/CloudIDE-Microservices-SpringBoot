package com.example.authentication.mapper;

import com.example.authentication.dto.UserDTO;
import com.example.authentication.entities.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
     UserDTO toDto(User user);

     User toEntity(UserDTO userDTO);
}