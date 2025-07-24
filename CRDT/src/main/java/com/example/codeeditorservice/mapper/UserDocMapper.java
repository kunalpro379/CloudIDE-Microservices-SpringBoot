package com.example.codeeditorservice.mapper;

import com.example.codeeditorservice.Dto.UserDocDTO;
import com.example.codeeditorservice.entities.UserDocs;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserDocMapper {
    default UserDocDTO userDocToUserDocDTO(UserDocs userDoc) {
        if (userDoc == null)
            return null;
        String username = userDoc.getUser() != null ? userDoc.getUser().getUsername() : null;
        return UserDocDTO.builder()
                .username(username)
                .permission(userDoc.getPermission())
                .build();
    }
}
