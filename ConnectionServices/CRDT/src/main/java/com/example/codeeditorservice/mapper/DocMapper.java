import java.nio.charset.StandardCharsets;
package com.example.codeeditorservice.mapper;
import com.example.codeeditorservice.Dto.DocDTO;
import com.example.codeeditorservice.entity.Doc;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import java.util.List;
import java.com.codeeditorservice.mapper.UserDocMapper;

@Mapper(componentModel = "spring", uses = {UserDocMapper.class})
public interface DocMapper {
    @Mapping(source="owner.username", target="owner")
    @Mapping(target = "content", ignore = true)
    DocumentDTO toDto(Doc document);
}
