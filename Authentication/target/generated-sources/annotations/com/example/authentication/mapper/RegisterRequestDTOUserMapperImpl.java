package com.example.authentication.mapper;

import com.example.authentication.dto.RegisterRequestDTO;
import com.example.authentication.entities.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-24T22:55:14+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.42.50.v20250628-1110, environment: Java 21.0.7 (Eclipse Adoptium)"
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
        user.firstName( registerRequestDTO.getFirstName() );
        user.lastName( registerRequestDTO.getLastName() );
        user.profileImageUrl( registerRequestDTO.getProfileImageUrl() );
        user.username( registerRequestDTO.getUsername() );

        return user.build();
    }
}
