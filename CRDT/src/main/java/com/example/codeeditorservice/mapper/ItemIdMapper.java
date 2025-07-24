package com.example.codeeditorservice.mapper;

import com.example.codeeditorservice.engine.Item;
import org.mapstruct.Named;

public class ItemIdMapper {
     @Named("itemToId")
     public String itemToId(Item value) {
          return value != null ? value.getId() : null;
     }
}