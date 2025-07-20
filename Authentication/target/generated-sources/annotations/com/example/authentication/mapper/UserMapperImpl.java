package com.example.authentication.mapper;

import com.example.authentication.dto.UserDTO;
import com.example.authentication.entities.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-20T23:35:42+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.9 (OpenLogic)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserDTO toDto(User user) {
        if ( user == null ) {
            return null;
        }

        UserDTO.UserDTOBuilder userDTO = UserDTO.builder();

        userDTO.userId( user.getUserId() );
        userDTO.username( user.getUsername() );
        userDTO.email( user.getEmail() );
        userDTO.firstName( user.getFirstName() );
        userDTO.lastName( user.getLastName() );
        userDTO.profileImageUrl( user.getProfileImageUrl() );
        userDTO.emailVerified( user.getEmailVerified() );
        userDTO.isActive( user.getIsActive() );
        userDTO.oauthProvider( user.getOauthProvider() );
        userDTO.createdAt( user.getCreatedAt() );
        userDTO.updatedAt( user.getUpdatedAt() );
        userDTO.lastLogin( user.getLastLogin() );

        return userDTO.build();
    }

    @Override
    public User toEntity(UserDTO userDTO) {
        if ( userDTO == null ) {
            return null;
        }

        User.UserBuilder user = User.builder();

        user.userId( userDTO.getUserId() );
        user.email( userDTO.getEmail() );
        user.username( userDTO.getUsername() );
        user.firstName( userDTO.getFirstName() );
        user.lastName( userDTO.getLastName() );
        user.profileImageUrl( userDTO.getProfileImageUrl() );
        user.emailVerified( userDTO.getEmailVerified() );
        user.isActive( userDTO.getIsActive() );
        user.oauthProvider( userDTO.getOauthProvider() );
        user.createdAt( userDTO.getCreatedAt() );
        user.updatedAt( userDTO.getUpdatedAt() );
        user.lastLogin( userDTO.getLastLogin() );

        return user.build();
    }
}
