import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
package com.example.codeeditorservice.Dto;

@Data
public class DocTitleDTO{
    @NonNull(message = "Document ID cannot be null")
    @JsonProperty(required = true)
    private String title;
}