package com.example.codeeditorservice.Dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DocumentChangeDTO {
    private String id;
    private String left;
    private String right;
    private String content;
    private String operation;
    
    @JsonIgnore
    private boolean isDeleted;

    @JsonIgnore
    private boolean isBold;

    @JsonIgnore
    private boolean isItalic;
}