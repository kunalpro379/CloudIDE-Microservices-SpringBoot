package com.example.codeeditorservice.mapper;

import com.example.codeeditorservice.Dto.UserDocDTO;
import com.example.codeeditorservice.model.UserDoc;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.codeeditorservice.repository.UserRepository;
import com.example.codeeditorservice.repository.DocumentRepository;
import com.example.codeeditorservice.entities.User  ;
import com.example.codeeditorservice.entities.Permission;
import com.example.codeeditorservice.entities.Document;

@Mapper(componentModel = "spring")
public interface UserDocMapper{
    @Mapping(target = "username", source = "user.username")
    UserDocDTO userDocToUserDocDTO(UserDoc userDoc);
}
