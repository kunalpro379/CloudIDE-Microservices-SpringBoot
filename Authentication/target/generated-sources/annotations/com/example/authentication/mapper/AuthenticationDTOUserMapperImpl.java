package com.example.authentication.mapper;

import com.example.authentication.dto.AuthenticationRequestDTO;
import com.example.authentication.entities.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-20T23:35:42+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.9 (OpenLogic)"
)
@Component
public class AuthenticationDTOUserMapperImpl implements AuthenticationDTOUserMapper {

    @Override
    public User authenticationRequestDTOToUser(AuthenticationRequestDTO authenticationRequestDTO) {
        if ( authenticationRequestDTO == null ) {
            return null;
        }

        User.UserBuilder user = User.builder();

        user.email( authenticationRequestDTO.getEmail() );
        user.username( authenticationRequestDTO.getUsername() );

        return user.build();
    }
}
