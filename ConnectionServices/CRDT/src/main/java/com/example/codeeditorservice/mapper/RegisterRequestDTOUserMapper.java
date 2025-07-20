package com.example.codeeditorservice.mapper;
import com.example.codeeditorservice.Dto.UserDocDTO;
import com.example.codeeditorservice.model.UserDoc;
import org.mapstruct.Mapper;

import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import java.util.List;
import org.springframework.stereotype.Component;
@Mapper(componantModel = "spring")
public interface RegisterRequestDTOUserMapper {
    User registerRequestDTOToUser(RegisterRequestDTO  registerRequestDTO );
}