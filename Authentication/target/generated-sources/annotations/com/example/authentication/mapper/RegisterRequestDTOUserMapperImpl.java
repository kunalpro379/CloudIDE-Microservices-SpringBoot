package com.example.authentication.mapper;

import com.example.authentication.dto.RegisterRequestDTO;
import com.example.authentication.entities.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-20T23:35:42+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.9 (OpenLogic)"
)
@Component
public class RegisterRequestDTOUserMapperImpl implements RegisterRequestDTOUserMapper {

    @Override
    public User registerRequestDTOToUser(RegisterRequestDTO registerRequestDTO) {
        if ( registerRequestDTO == null ) {
            return null;
        }

        User.UserBuilder user = User.builder();

        user.email( registerRequestDTO.getEmail() );
        user.username( registerRequestDTO.getUsername() );
        user.firstName( registerRequestDTO.getFirstName() );
        user.lastName( registerRequestDTO.getLastName() );
        user.profileImageUrl( registerRequestDTO.getProfileImageUrl() );

        return user.build();
    }
}
