package com.example.codeeditorservice.mapper;

import com.example.codeeditorservice.Dto.AuthenticationRequestDTO;
import com.example.codeeditorservice.entities.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-25T00:35:59+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.9 (OpenLogic)"
)
@Component
public class AuthenticationDTOUserMapperImpl implements AuthenticationDTOUserMapper {

    @Override
    public User authenticationRequestDTOToUser(AuthenticationRequestDTO authenticationRequestDTO) {
        if ( authenticationRequestDTO == null ) {
            return null;
        }

        User user = new User();

        return user;
    }
}
