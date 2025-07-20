import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
package com.example.codeeditorservice.mapper;
import java.util.List;
import org.example.codeeditorservice.Dto.DocumentChangeDTO;

@Mapper(componantModel = "spring")
public interface DocChangeMapper  {
    @Mapping(source = "right.id", target = "right")
    @Mapping(source = "left.id", target = "left")
    List<DocumentChangeDTO>toDto(List<Item>items);
    default String map(Item value){
        return value !=null?value.getId():null;
    }
}
