package com.example.codeeditorservice.mapper;

import com.example.codeeditorservice.Dto.DocumentDTO;
import com.example.codeeditorservice.entities.Doc;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = { UserDocMapper.class })
public interface DocMapper {
    default DocumentDTO toDto(Doc document) {
        if (document == null)
            return null;
        String owner = document.getOwner() != null ? document.getOwner().getUsername() : null;
        String content = document.getContent() != null ? new String(document.getContent()) : null;
        return DocumentDTO.builder()
                .id(document.getId())
                .owner(owner)
                .title(document.getTitle())
                .content(content)
                .sharedWith(null) // Map sharedWith if needed
                .build();
    }
}
