package com.example.authentication.mapper;

import com.example.authentication.dto.UserDTO;
import com.example.authentication.entities.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-24T22:55:14+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.42.50.v20250628-1110, environment: Java 21.0.7 (Eclipse Adoptium)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserDTO toDto(User user) {
        if ( user == null ) {
            return null;
        }

        UserDTO.UserDTOBuilder userDTO = UserDTO.builder();

        userDTO.createdAt( user.getCreatedAt() );
        userDTO.email( user.getEmail() );
        userDTO.emailVerified( user.getEmailVerified() );
        userDTO.firstName( user.getFirstName() );
        userDTO.isActive( user.getIsActive() );
        userDTO.lastLogin( user.getLastLogin() );
        userDTO.lastName( user.getLastName() );
        userDTO.oauthProvider( user.getOauthProvider() );
        userDTO.profileImageUrl( user.getProfileImageUrl() );
        userDTO.updatedAt( user.getUpdatedAt() );
        userDTO.userId( user.getUserId() );
        userDTO.username( user.getUsername() );

        return userDTO.build();
    }

    @Override
    public User toEntity(UserDTO userDTO) {
        if ( userDTO == null ) {
            return null;
        }

        User.UserBuilder user = User.builder();

        user.createdAt( userDTO.getCreatedAt() );
        user.email( userDTO.getEmail() );
        user.emailVerified( userDTO.getEmailVerified() );
        user.firstName( userDTO.getFirstName() );
        user.isActive( userDTO.getIsActive() );
        user.lastLogin( userDTO.getLastLogin() );
        user.lastName( userDTO.getLastName() );
        user.oauthProvider( userDTO.getOauthProvider() );
        user.profileImageUrl( userDTO.getProfileImageUrl() );
        user.updatedAt( userDTO.getUpdatedAt() );
        user.userId( userDTO.getUserId() );
        user.username( userDTO.getUsername() );

        return user.build();
    }
}
