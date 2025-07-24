package com.example.codeeditorservice.Dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DocumentDTO {
    private Long id;
    private String owner;
    private String title;
    private String content;
    private List<UserDocDTO> sharedWith;
}