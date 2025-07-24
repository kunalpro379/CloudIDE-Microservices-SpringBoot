package com.example.codeeditorservice.mapper;

import com.example.codeeditorservice.Dto.DocumentChangeDTO;
import com.example.codeeditorservice.engine.Item;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DocChangeMapper {

    public DocumentChangeDTO toDto(Item item) {
        if (item == null) {
            return null;
        }
        
        return DocumentChangeDTO.builder()
                .id(item.getId())
                .left(item.getLeft() != null ? item.getLeft().getId() : null)
                .right(item.getRight() != null ? item.getRight().getId() : null)
                .content(item.getContent())
                .operation(item.getOperation())
                .isDeleted(item.isDeleted())
                .isBold(item.isBold())
                .isItalic(item.isItalic())
                .build();
    }

    public List<DocumentChangeDTO> toDto(List<Item> items) {
        if (items == null) {
            return null;
        }
        
        return items.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
