package com.example.codeeditorservice.Dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NonNull;

@Data
public class DocTitleDTO {
    @NonNull
    @JsonProperty(required = true)
    private String title;
}